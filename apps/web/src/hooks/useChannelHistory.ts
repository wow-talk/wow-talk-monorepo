"use client";

import { useQuery } from "@tanstack/react-query";

import { getChannelMessages } from "@/lib/api/messages";
import type { RoomId } from "@/types/domain";

/**
 * 채널의 최근 메시지 N개 (REST 초기 로드용).
 * WS 실시간 메시지는 별도 store/state에서 누적. useChatHistory가 둘을 합친다.
 */
export function useChannelHistory(roomId: RoomId, limit = 50) {
  return useQuery({
    queryKey: ["channel", roomId, "messages", limit],
    queryFn: () => getChannelMessages(roomId, limit),
    enabled: Boolean(roomId),
  });
}
