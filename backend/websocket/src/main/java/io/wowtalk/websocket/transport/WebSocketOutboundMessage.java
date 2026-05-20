package io.wowtalk.websocket.transport;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.transport.TransportMessage;

public record WebSocketOutboundMessage(
        String type,
        String roomId,
        String sessionId,
        String payload,
        String sentAt,
        String code,
        String message
) {

    public static WebSocketOutboundMessage connected(String roomId, String sessionId) {
        return new WebSocketOutboundMessage(
                WebSocketMessageType.CONNECTED.name(),
                roomId,
                sessionId,
                "웹소켓 연결이 완료되었습니다.",
                null,
                null,
                null
        );
    }

    public static WebSocketOutboundMessage chatMessage(TransportMessage message) {
        return new WebSocketOutboundMessage(
                WebSocketMessageType.CHAT_MESSAGE.name(),
                message.roomId().value(),
                message.sessionId().value(),
                message.payload(),
                message.sentAt().toString(),
                null,
                null
        );
    }

    public static WebSocketOutboundMessage error(ErrorCode errorCode) {
        return new WebSocketOutboundMessage(
                WebSocketMessageType.ERROR.name(),
                null,
                null,
                null,
                null,
                errorCode.name(),
                errorCode.message()
        );
    }
}
