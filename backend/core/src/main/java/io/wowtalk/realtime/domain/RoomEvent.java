package io.wowtalk.realtime.domain;

import io.wowtalk.transport.RoomId;
import java.time.Instant;

public record RoomEvent(
        EventId eventId,
        RoomId roomId,
        RoomEventType eventType,
        String actorUserId,
        String payload,
        Instant occurredAt
) {

    public RoomEvent {
        if (eventId == null) {
            eventId = EventId.newId();
        }
        if (roomId == null) {
            throw new IllegalArgumentException("방 ID는 필수입니다.");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("이벤트 타입은 필수입니다.");
        }
        if (payload == null || payload.isBlank()) {
            payload = "{}";
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
