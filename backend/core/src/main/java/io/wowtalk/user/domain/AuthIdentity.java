package io.wowtalk.user.domain;

import java.time.Instant;

/**
 * 외부 인증 제공자 또는 guest credential과 내부 User를 연결하는 도메인 모델이다.
 *
 * <p>서비스 내부에서는 UserId를 기준으로 동작하고, Google/Kakao/Cognito 같은 provider의 subject는
 * AuthIdentity에 격리한다. 이렇게 해야 guest user를 나중에 소셜 계정과 연결해도 기존 userId를
 * 유지할 수 있다.
 */
public record AuthIdentity(
        AuthIdentityId authIdentityId,
        UserId userId,
        AuthProvider provider,
        String providerSubject,
        Instant createdAt
) {

    public AuthIdentity {
        if (authIdentityId == null) {
            authIdentityId = AuthIdentityId.newId();
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (provider == null) {
            throw new IllegalArgumentException("인증 제공자는 필수입니다.");
        }
        if (providerSubject == null || providerSubject.isBlank()) {
            throw new IllegalArgumentException("인증 제공자 subject는 비어 있을 수 없습니다.");
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
