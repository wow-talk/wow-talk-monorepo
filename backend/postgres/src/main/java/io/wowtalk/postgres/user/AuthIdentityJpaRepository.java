package io.wowtalk.postgres.user;

import io.wowtalk.user.domain.AuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthIdentityJpaRepository extends JpaRepository<AuthIdentityEntity, Long> {

    Optional<AuthIdentityEntity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
