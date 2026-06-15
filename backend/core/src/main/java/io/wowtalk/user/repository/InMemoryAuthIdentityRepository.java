package io.wowtalk.user.repository;

import io.wowtalk.user.domain.AuthIdentity;
import io.wowtalk.user.domain.AuthProvider;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAuthIdentityRepository implements AuthIdentityRepository {

    private final Map<String, AuthIdentity> identities = new ConcurrentHashMap<>();

    @Override
    public Optional<AuthIdentity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject) {
        return Optional.ofNullable(identities.get(key(provider, providerSubject)));
    }

    @Override
    public AuthIdentity save(AuthIdentity authIdentity) {
        identities.put(key(authIdentity.provider(), authIdentity.providerSubject()), authIdentity);
        return authIdentity;
    }

    private String key(AuthProvider provider, String providerSubject) {
        return provider.name() + "#" + providerSubject;
    }
}
