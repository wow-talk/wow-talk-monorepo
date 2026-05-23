import { apiFetch } from "@/lib/api/client";
import { ChannelSchema, type Channel } from "@/types/api";
import type { RoomId } from "@/types/domain";

/**
 * 채널 생성 또는 보장.
 *
 * 백엔드 POST /api/v1/channels: roomId가 없으면 생성하고, 있으면 transportMode 일치 여부만 확인.
 * 불일치 시 TRANSPORT_MODE_MISMATCH 에러를 ApiError로 throw.
 */
export async function ensureChannel(roomId: RoomId): Promise<Channel> {
  return apiFetch("/api/v1/channels", ChannelSchema, {
    method: "POST",
    body: JSON.stringify({ roomId, transportMode: "WEBSOCKET" }),
  });
}

export async function getChannel(roomId: RoomId): Promise<Channel> {
  return apiFetch(
    `/api/v1/channels/${encodeURIComponent(roomId)}`,
    ChannelSchema,
  );
}
