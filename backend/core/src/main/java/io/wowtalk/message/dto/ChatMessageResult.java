package io.wowtalk.message.dto;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import java.time.Instant;

public record ChatMessageResult(
        RoomId roomId,
        SessionId sessionId,
        String payload,
        Instant sentAt
) {
}
