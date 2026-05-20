package io.wowtalk.message.domain;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import java.time.Instant;

public record ChatMessage(
        RoomId roomId,
        SessionId sessionId,
        String payload,
        Instant sentAt
) {
}
