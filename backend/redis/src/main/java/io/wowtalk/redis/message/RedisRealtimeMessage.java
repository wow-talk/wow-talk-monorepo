package io.wowtalk.redis.message;

import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.transport.TransportMessage;
import java.time.Instant;

public record RedisRealtimeMessage(
        String messageId,
        String roomId,
        String connectionId,
        String sessionId,
        String senderUserId,
        String payload,
        String sentAt,
        String requestId
) {

    public static RedisRealtimeMessage from(TransportMessage message) {
        return new RedisRealtimeMessage(
                message.messageId(),
                message.roomId().value(),
                message.connectionId().value(),
                message.sessionId().value(),
                message.senderUserId(),
                message.payload(),
                message.sentAt().toString(),
                message.requestId()
        );
    }

    public TransportMessage toTransportMessage() {
        return new TransportMessage(
                messageId,
                new RoomId(roomId),
                new ConnectionId(connectionId),
                new SessionId(sessionId),
                senderUserId,
                payload,
                Instant.parse(sentAt),
                requestId
        );
    }
}
