package io.wowtalk.user.domain;

public record User(
        UserId userId,
        UserType userType,
        String displayName
) {

    public User {
        if (userId == null) {
            userId = UserId.newId();
        }
        if (userType == null) {
            throw new IllegalArgumentException("사용자 유형은 필수입니다.");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("표시 이름은 비어 있을 수 없습니다.");
        }
    }
}
