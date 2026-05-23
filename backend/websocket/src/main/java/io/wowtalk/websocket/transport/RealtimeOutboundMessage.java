package io.wowtalk.websocket.transport;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.transport.TransportMessage;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RealtimeOutboundMessage(
        int version,
        String type,
        String eventId,
        String requestId,
        String roomId,
        String occurredAt,
        Map<String, Object> payload
) {

    public static RealtimeOutboundMessage connected(String roomId, String connectionId, String sessionId, String userId) {
        return new RealtimeOutboundMessage(
                1,
                WebSocketMessageType.CONNECTED.name(),
                newEventId(),
                null,
                roomId,
                Instant.now().toString(),
                Map.of(
                        "connectionId", connectionId,
                        "sessionId", sessionId,
                        "userId", userId
                )
        );
    }

    public static RealtimeOutboundMessage chatMessageCreated(TransportMessage message) {
        return new RealtimeOutboundMessage(
                1,
                WebSocketMessageType.CHAT_MESSAGE_CREATED.name(),
                newEventId(),
                message.requestId(),
                message.roomId().value(),
                message.sentAt().toString(),
                Map.of(
                        "messageId", message.messageId(),
                        "connectionId", message.connectionId().value(),
                        "sessionId", message.sessionId().value(),
                        "senderUserId", message.senderUserId(),
                        "text", message.payload()
                )
        );
    }

    public static RealtimeOutboundMessage error(ErrorCode errorCode) {
        return new RealtimeOutboundMessage(
                1,
                WebSocketMessageType.ERROR.name(),
                newEventId(),
                null,
                null,
                Instant.now().toString(),
                Map.of(
                        "code", errorCode.name(),
                        "message", errorCode.message()
                )
        );
    }

    private static String newEventId() {
        return UUID.randomUUID().toString();
    }
}
