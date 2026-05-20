import { env } from "@/lib/env";
import { InboundMessageSchema } from "@/lib/ws/schemas";
import type { InboundMessage, OutboundMessage } from "@/types/ws";

/**
 * WebSocket 라이프사이클의 단일 소유자.
 *
 * 컴포넌트는 절대 직접 `new WebSocket()`을 호출하지 말고 이 클래스를 통한다.
 * React에서는 `useChatSocket` 훅이 인스턴스 한 개를 effect에 묶어 마운트/언마운트와 동기화한다.
 *
 * - 메시지 수신은 Zod `safeParse` → 실패 시 parse-error 이벤트만 발행하고 채팅 UI 비노출.
 * - 비정상 close 시 지수 백오프(500/1000/2000/4000/8000ms, 최대 5회) 재연결.
 * - `disconnect()` 호출 또는 정상 close 시 재연결 안 함.
 */

export type WsStatus = "idle" | "connecting" | "open" | "closed" | "error";

export type WsEvent =
  | { type: "status"; status: WsStatus }
  | { type: "message"; message: InboundMessage }
  | { type: "parse-error"; raw: string; reason: string }
  | { type: "retry"; attempt: number; delayMs: number; closeCode: number };

type Listener = (event: WsEvent) => void;

const BACKOFF_DELAYS_MS = [500, 1000, 2000, 4000, 8000] as const;
const MAX_RETRIES = BACKOFF_DELAYS_MS.length;

export class WsClient {
  private socket: WebSocket | null = null;
  private params: { roomId: string; sessionId: string } | null = null;
  private listeners = new Set<Listener>();
  private retries = 0;
  private retryTimer: ReturnType<typeof setTimeout> | null = null;
  private reconnect = true;
  private destroyed = false;

  on(listener: Listener): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  connect(params: { roomId: string; sessionId: string }): void {
    if (this.destroyed) return;
    if (!params.roomId.trim() || !params.sessionId.trim()) {
      this.emit({
        type: "parse-error",
        raw: "",
        reason: "roomId / sessionId must be non-blank",
      });
      return;
    }
    this.params = params;
    this.reconnect = true;
    this.retries = 0;
    this.clearRetry();
    this.closeSocket();
    this.open();
  }

  send(message: OutboundMessage): boolean {
    if (this.socket?.readyState !== WebSocket.OPEN) return false;
    this.socket.send(JSON.stringify(message));
    return true;
  }

  disconnect(): void {
    this.reconnect = false;
    this.clearRetry();
    this.closeSocket();
  }

  dispose(): void {
    this.destroyed = true;
    this.reconnect = false;
    this.clearRetry();
    this.closeSocket();
    this.listeners.clear();
  }

  private open(): void {
    if (this.destroyed || !this.params) return;

    const url = `${env.NEXT_PUBLIC_WS_BASE}/ws/chat?roomId=${encodeURIComponent(
      this.params.roomId,
    )}&sessionId=${encodeURIComponent(this.params.sessionId)}`;

    this.emit({ type: "status", status: "connecting" });

    const socket = new WebSocket(url);
    this.socket = socket;

    socket.onopen = () => {
      this.retries = 0;
      this.emit({ type: "status", status: "open" });
    };

    socket.onmessage = (event) => {
      const raw = typeof event.data === "string" ? event.data : "";
      let json: unknown;
      try {
        json = JSON.parse(raw);
      } catch {
        this.emit({ type: "parse-error", raw, reason: "JSON parse failed" });
        return;
      }
      const parsed = InboundMessageSchema.safeParse(json);
      if (!parsed.success) {
        this.emit({
          type: "parse-error",
          raw,
          reason: parsed.error.issues
            .map((i) => `${i.path.join(".")}: ${i.message}`)
            .join("; "),
        });
        return;
      }
      this.emit({ type: "message", message: parsed.data });
    };

    socket.onerror = () => {
      this.emit({ type: "status", status: "error" });
    };

    socket.onclose = (event) => {
      this.emit({ type: "status", status: "closed" });
      if (this.destroyed) return;
      if (!this.reconnect) return;
      if (event.wasClean) return;
      if (this.retries >= MAX_RETRIES) return;

      const delayMs =
        BACKOFF_DELAYS_MS[this.retries] ??
        BACKOFF_DELAYS_MS[MAX_RETRIES - 1];
      this.retries += 1;
      this.emit({
        type: "retry",
        attempt: this.retries,
        delayMs,
        closeCode: event.code,
      });
      this.retryTimer = setTimeout(() => {
        this.retryTimer = null;
        this.open();
      }, delayMs);
    };
  }

  private closeSocket(): void {
    const s = this.socket;
    if (!s) return;
    this.socket = null;
    // 이벤트 핸들러 제거 후 close — 닫는 도중 onclose가 두 번 발화하는 사고 방지
    s.onopen = null;
    s.onmessage = null;
    s.onerror = null;
    s.onclose = null;
    if (s.readyState === WebSocket.OPEN || s.readyState === WebSocket.CONNECTING) {
      s.close();
    }
  }

  private clearRetry(): void {
    if (this.retryTimer) {
      clearTimeout(this.retryTimer);
      this.retryTimer = null;
    }
  }

  private emit(event: WsEvent): void {
    for (const listener of this.listeners) listener(event);
  }
}
