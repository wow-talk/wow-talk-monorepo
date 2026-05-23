/**
 * WebSocket 메시지 타입 정의.
 *
 * 백엔드 wowtalk-websocket 모듈의 WebSocketInboundMessage / WebSocketOutboundMessage와 1:1 대응.
 * Zod 스키마(`src/lib/ws/schemas.ts`)에서 z.infer로 동일 타입을 재추론할 수 있지만,
 * 외부 의존성 0 원칙을 위해 도메인 측에는 순수 TS 타입만 둔다.
 */

export type ErrorCode =
  | "WEBSOCKET_CONNECTION_INVALID"
  | "INVALID_WEBSOCKET_MESSAGE_FORMAT"
  | "UNSUPPORTED_MESSAGE_TYPE"
  | "INVALID_CHAT_MESSAGE"
  | "CHANNEL_NOT_FOUND"
  | "TRANSPORT_MODE_MISMATCH";

export interface ConnectedMessage {
  type: "CONNECTED";
  roomId: string;
  sessionId: string;
  payload: string;
  sentAt: null;
  code: null;
  message: null;
}

export interface ChatMessage {
  type: "CHAT_MESSAGE";
  roomId: string;
  sessionId: string;
  payload: string;
  sentAt: string;
  code: null;
  message: null;
}

export interface ErrorMessage {
  type: "ERROR";
  roomId: null;
  sessionId: null;
  payload: null;
  sentAt: null;
  code: ErrorCode;
  message: string;
}

export type InboundMessage = ConnectedMessage | ChatMessage | ErrorMessage;

export interface SendMessage {
  type: "SEND_MESSAGE";
  payload: string;
}

export type OutboundMessage = SendMessage;
