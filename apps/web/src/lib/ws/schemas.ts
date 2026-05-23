import { z } from "zod";

/**
 * 백엔드 wowtalk-websocket의 outbound 메시지 4종을 Zod로 검증.
 * discriminatedUnion을 쓰면 type 필드로 정확한 sub-스키마가 선택되고,
 * parse 후 타입 좁히기까지 자동으로 따라온다.
 */

const ErrorCodeSchema = z.enum([
  "WEBSOCKET_CONNECTION_INVALID",
  "INVALID_WEBSOCKET_MESSAGE_FORMAT",
  "UNSUPPORTED_MESSAGE_TYPE",
  "INVALID_CHAT_MESSAGE",
  "CHANNEL_NOT_FOUND",
  "TRANSPORT_MODE_MISMATCH",
]);

const ConnectedMessageSchema = z.object({
  type: z.literal("CONNECTED"),
  roomId: z.string(),
  sessionId: z.string(),
  payload: z.string(),
  sentAt: z.null(),
  code: z.null(),
  message: z.null(),
});

const ChatMessageSchema = z.object({
  type: z.literal("CHAT_MESSAGE"),
  roomId: z.string().min(1),
  sessionId: z.string().min(1),
  payload: z.string(),
  sentAt: z.string(),
  code: z.null(),
  message: z.null(),
});

const ErrorMessageSchema = z.object({
  type: z.literal("ERROR"),
  roomId: z.null(),
  sessionId: z.null(),
  payload: z.null(),
  sentAt: z.null(),
  code: ErrorCodeSchema,
  message: z.string(),
});

export const InboundMessageSchema = z.discriminatedUnion("type", [
  ConnectedMessageSchema,
  ChatMessageSchema,
  ErrorMessageSchema,
]);

export type ParsedInboundMessage = z.infer<typeof InboundMessageSchema>;
