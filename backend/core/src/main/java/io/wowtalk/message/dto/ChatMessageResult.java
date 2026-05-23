package io.wowtalk.message.dto;

import io.wowtalk.message.domain.MessageId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import java.time.Instant;

public record ChatMessageResult(
        MessageId messageId,
        RoomId roomId,
        SessionId sessionId,
        String payload,
        Instant sentAt
) {
}
