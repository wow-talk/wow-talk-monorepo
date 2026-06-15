package io.wowtalk.user.repository;

import io.wowtalk.user.domain.AuthIdentity;
import io.wowtalk.user.domain.AuthProvider;
import java.util.Optional;

/**
 * 외부 인증 주체와 내부 User 연결 정보를 저장하는 core 영속성 포트다.
 *
 * <p>providerSubject는 Cognito sub, Google subject, guest credential처럼 provider 안에서 유일한 값을
 * 의미한다. core는 이 값의 발급 방식을 알지 않는다.
 */
public interface AuthIdentityRepository {

    Optional<AuthIdentity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    AuthIdentity save(AuthIdentity authIdentity);
}
