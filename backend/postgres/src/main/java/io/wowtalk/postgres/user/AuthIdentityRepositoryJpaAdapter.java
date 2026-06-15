package io.wowtalk.postgres.user;

import io.wowtalk.user.domain.AuthIdentity;
import io.wowtalk.user.domain.AuthIdentityId;
import io.wowtalk.user.domain.AuthProvider;
import io.wowtalk.user.domain.UserId;
import io.wowtalk.user.repository.AuthIdentityRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Profile("postgres")
@Primary
@Repository
public class AuthIdentityRepositoryJpaAdapter implements AuthIdentityRepository {

    private final AuthIdentityJpaRepository authIdentityJpaRepository;

    public AuthIdentityRepositoryJpaAdapter(AuthIdentityJpaRepository authIdentityJpaRepository) {
        this.authIdentityJpaRepository = authIdentityJpaRepository;
    }

    @Override
    public Optional<AuthIdentity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject) {
        return authIdentityJpaRepository.findByProviderAndProviderSubject(provider, providerSubject)
                .map(this::toDomain);
    }

    @Override
    public AuthIdentity save(AuthIdentity authIdentity) {
        AuthIdentityEntity saved = authIdentityJpaRepository.save(new AuthIdentityEntity(
                authIdentity.authIdentityId().value(),
                authIdentity.userId().value(),
                authIdentity.provider(),
                authIdentity.providerSubject(),
                authIdentity.createdAt()
        ));
        return toDomain(saved);
    }

    private AuthIdentity toDomain(AuthIdentityEntity entity) {
        return new AuthIdentity(
                new AuthIdentityId(entity.getAuthIdentityId()),
                new UserId(entity.getUserId()),
                entity.getProvider(),
                entity.getProviderSubject(),
                entity.getCreatedAt()
        );
    }
}
