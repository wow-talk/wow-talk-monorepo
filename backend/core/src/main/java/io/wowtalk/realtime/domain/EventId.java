package io.wowtalk.realtime.domain;

import java.util.UUID;

public record EventId(String value) {

    public EventId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이벤트 ID는 필수입니다.");
        }
    }

    public static EventId newId() {
        return new EventId(UUID.randomUUID().toString());
    }
}
