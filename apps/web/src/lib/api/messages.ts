import { apiFetch } from "@/lib/api/client";
import { ChatMessageHistorySchema, type ChatMessageHistory } from "@/types/api";
import type { RoomId } from "@/types/domain";

/**
 * 채널의 최근 메시지 N개 조회.
 *
 * 백엔드는 오래된 순으로 정렬해 반환한다.
 * 채널이 없으면 CHANNEL_NOT_FOUND → ApiError(404).
 */
export async function getChannelMessages(
  roomId: RoomId,
  limit = 50,
): Promise<ChatMessageHistory> {
  const query = new URLSearchParams({ limit: String(limit) }).toString();
  return apiFetch(
    `/api/v1/channels/${encodeURIComponent(roomId)}/messages?${query}`,
    ChatMessageHistorySchema,
  );
}
