package io.wowtalk.user.domain;

/**
 * Wow Talk 안에서 메시지 발신자와 방 참여자로 쓰이는 사용자 모델이다.
 *
 * <p>초기에는 guest user를 중심으로 사용하고, 이후 social account linking 또는 Cognito 같은
 * 외부 인증 주체와 연결할 수 있게 UserId를 내부 식별자로 유지한다.
 */
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
