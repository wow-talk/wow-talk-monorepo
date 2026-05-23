"use client";

import { useMemo } from "react";

import { useChannelHistory } from "@/hooks/useChannelHistory";
import { useChatSocket } from "@/lib/ws/useChatSocket";
import type { ChatMessageResult } from "@/types/api";

/**
 * REST 초기 히스토리 + WS 실시간 메시지를 하나의 시간순 배열로 합친다.
 *
 * - REST 히스토리는 mount 시 한 번만 fetch (TanStack Query 캐시).
 * - WS는 useChatSocket 훅이 라이프사이클 소유. liveMessages가 누적된다.
 * - 합치는 규칙: REST 히스토리 끝의 sentAt보다 늦은 WS 메시지만 뒤에 붙여 중복 제거.
 *   (백엔드가 발신자에게도 자신의 메시지를 브로드캐스트하므로 본인 메시지도 자연스럽게 표시됨)
 */
export function useChatHistory(roomId: string, sessionId: string) {
  const history = useChannelHistory(roomId);
  const socket = useChatSocket({ roomId, sessionId });

  const messages = useMemo<ChatMessageResult[]>(() => {
    const rest = history.data ?? [];
    const restLast = rest.length > 0 ? rest[rest.length - 1]!.sentAt : null;
    const live = socket.liveMessages
      .filter((m) => (restLast ? m.sentAt > restLast : true))
      .map<ChatMessageResult>((m) => ({
        roomId: m.roomId,
        sessionId: m.sessionId,
        payload: m.payload,
        sentAt: m.sentAt,
      }));
    return [...rest, ...live];
  }, [history.data, socket.liveMessages]);

  return {
    messages,
    isHistoryPending: history.isPending,
    historyError: history.error,
    socketStatus: socket.status,
    socketLog: socket.log,
    sendMessage: socket.send,
    disconnectSocket: socket.disconnect,
  };
}
