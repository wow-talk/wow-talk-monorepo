package io.wowtalk.realtime.domain;

import io.wowtalk.transport.RoomId;
import java.time.Instant;

/**
 * 채팅방 안에서 발생한 realtime 이벤트의 공통 기록 모델이다.
 *
 * <p>채팅 메시지, 게임 이벤트, 시스템 이벤트를 같은 room event stream에 올릴 수 있게 하기 위한
 * 기반 모델이다.
 */
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
