package io.wowtalk.message.domain;

import java.util.UUID;

public record MessageId(String value) {

    public MessageId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("메시지 ID는 비어 있을 수 없습니다.");
        }
    }

    public static MessageId newId() {
        return new MessageId(UUID.randomUUID().toString());
    }
}
