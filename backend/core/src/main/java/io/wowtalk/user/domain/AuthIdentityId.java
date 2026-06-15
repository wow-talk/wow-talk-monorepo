package io.wowtalk.user.domain;

import java.util.UUID;

public record AuthIdentityId(String value) {

    public AuthIdentityId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("인증 식별자 ID는 비어 있을 수 없습니다.");
        }
    }

    public static AuthIdentityId newId() {
        return new AuthIdentityId(UUID.randomUUID().toString());
    }
}
