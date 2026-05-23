package io.wowtalk.message.domain;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.user.domain.UserId;
import java.time.Instant;

public record ChatMessage(
        MessageId messageId,
        RoomId roomId,
        SessionId sessionId,
        UserId senderUserId,
        String payload,
        Instant sentAt
) {

    public ChatMessage {
        if (messageId == null) {
            messageId = MessageId.newId();
        }
        if (senderUserId == null) {
            senderUserId = new UserId(sessionId.value());
        }
    }
}
