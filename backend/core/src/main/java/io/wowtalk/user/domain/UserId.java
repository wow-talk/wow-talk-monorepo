package io.wowtalk.user.domain;

import java.util.UUID;

public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("사용자 ID는 비어 있을 수 없습니다.");
        }
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }
}
