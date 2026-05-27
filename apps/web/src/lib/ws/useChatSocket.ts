"use client";

import { useCallback, useEffect, useRef, useState } from "react";

import { publishInspectorLine } from "@/lib/inspector/bus";
import { WsClient, type WsStatus } from "@/lib/ws/wsClient";
import type { ChatMessage } from "@/types/ws";

/**
 * `WsClient`를 React 라이프사이클에 묶는 훅.
 *
 * - effect 안에서 인스턴스 생성/소유 → StrictMode 더블 마운트 클린업 안전.
 * - 외부에 노출하는 것: 현재 status, 누적된 실시간 메시지, lifecycle 로그, send/disconnect.
 * - log는 컴포넌트 내부용. 동시에 같은 이벤트를 inspectorBus로 fan-out해 inspectorStore에 자동 누적.
 */

export interface UseChatSocketParams {
  roomId: string;
  sessionId: string;
  enabled?: boolean;
}

export type ChatSocketLogKind =
  | "status"
  | "incoming"
  | "outgoing"
  | "parse-error"
  | "retry";

export interface ChatSocketLogEntry {
  id: string;
  timestamp: number;
  kind: ChatSocketLogKind;
  detail: string;
}

export interface UseChatSocketReturn {
  status: WsStatus;
  liveMessages: ChatMessage[];
  log: ChatSocketLogEntry[];
  send: (payload: string) => boolean;
  disconnect: () => void;
}

let logCounter = 0;
const makeLogId = (): string => `log-${++logCounter}`;

export function useChatSocket(
  params: UseChatSocketParams,
): UseChatSocketReturn {
  const { roomId, sessionId, enabled = true } = params;
  const clientRef = useRef<WsClient | null>(null);
  const [status, setStatus] = useState<WsStatus>("idle");
  const [liveMessages, setLiveMessages] = useState<ChatMessage[]>([]);
  const [log, setLog] = useState<ChatSocketLogEntry[]>([]);

  // React 19 권장: props 변경 시 state 리셋을 effect 안의 setState로 하지 않고,
  // render 중 직전 키와 비교 → 다르면 즉시 setState. React가 render를 즉시 재시작해 일관 상태를 보장.
  // https://react.dev/reference/react/useState#storing-information-from-previous-renders
  const currentKey = `${enabled ? "on" : "off"}|${roomId}|${sessionId}`;
  const [prevKey, setPrevKey] = useState(currentKey);
  if (prevKey !== currentKey) {
    setPrevKey(currentKey);
    setLiveMessages([]);
  }

  const appendLog = useCallback(
    (kind: ChatSocketLogKind, detail: string) => {
      setLog((prev) => [
        ...prev,
        { id: makeLogId(), timestamp: Date.now(), kind, detail },
      ]);
      // 같은 이벤트를 inspector bus로 fan-out. kind는 InspectorLineKind와 호환되는 subset.
      publishInspectorLine({ kind, text: detail });
    },
    [],
  );

  useEffect(() => {
    if (!enabled) return;
    if (!roomId.trim() || !sessionId.trim()) return;

    const client = new WsClient();
    clientRef.current = client;

    const unsubscribe = client.on((event) => {
      if (event.type === "status") {
        setStatus(event.status);
        appendLog("status", `socket -> ${event.status}`);
      } else if (event.type === "message") {
        const msg = event.message;
        if (msg.type === "CHAT_MESSAGE") {
          setLiveMessages((prev) => [...prev, msg]);
          appendLog(
            "incoming",
            `CHAT_MESSAGE ${msg.sessionId}: ${msg.payload}`,
          );
        } else if (msg.type === "CONNECTED") {
          appendLog("incoming", `CONNECTED ${msg.sessionId} (${msg.payload})`);
        } else {
          appendLog("incoming", `ERROR ${msg.code} ${msg.message}`);
        }
      } else if (event.type === "parse-error") {
        appendLog("parse-error", event.reason);
      } else if (event.type === "retry") {
        appendLog(
          "retry",
          `attempt ${event.attempt} in ${event.delayMs}ms (close ${event.closeCode})`,
        );
      }
    });

    client.connect({ roomId, sessionId });

    return () => {
      unsubscribe();
      client.dispose();
      clientRef.current = null;
    };
  }, [roomId, sessionId, enabled, appendLog]);

  const send = useCallback(
    (payload: string): boolean => {
      const client = clientRef.current;
      if (!client) return false;
      const ok = client.send({ type: "SEND_MESSAGE", payload });
      if (ok) appendLog("outgoing", `SEND_MESSAGE ${payload}`);
      return ok;
    },
    [appendLog],
  );

  const disconnect = useCallback((): void => {
    clientRef.current?.disconnect();
  }, []);

  return { status, liveMessages, log, send, disconnect };
}
