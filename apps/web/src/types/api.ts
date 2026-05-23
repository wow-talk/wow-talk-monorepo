import { z } from "zod";

/**
 * REST API 응답 스키마.
 *
 * 백엔드 wowtalk-api 컨트롤러의 응답 shape과 1:1 대응.
 * apiFetch helper가 fetch 결과를 이 스키마로 parse → 계약 변경 시 즉시 throw.
 */

export const TransportModeSchema = z.enum(["WEBSOCKET", "RAW_TCP"]);

export const ChannelSchema = z.object({
  roomId: z.string().min(1),
  transportMode: TransportModeSchema,
});
export type Channel = z.infer<typeof ChannelSchema>;

export const ChatMessageResultSchema = z.object({
  roomId: z.string().min(1),
  sessionId: z.string().min(1),
  payload: z.string(),
  sentAt: z.string(),
});
export type ChatMessageResult = z.infer<typeof ChatMessageResultSchema>;

export const ChatMessageHistorySchema = z.array(ChatMessageResultSchema);
export type ChatMessageHistory = z.infer<typeof ChatMessageHistorySchema>;
