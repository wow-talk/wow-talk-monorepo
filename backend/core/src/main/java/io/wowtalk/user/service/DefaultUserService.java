package io.wowtalk.user.service;

import io.wowtalk.user.domain.AuthIdentity;
import io.wowtalk.user.domain.AuthProvider;
import io.wowtalk.user.domain.User;
import io.wowtalk.user.domain.UserId;
import io.wowtalk.user.domain.UserType;
import io.wowtalk.user.repository.AuthIdentityRepository;
import io.wowtalk.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultUserService implements UserService {

    private static final String DEFAULT_GUEST_DISPLAY_NAME = "guest";

    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;

    public DefaultUserService(UserRepository userRepository, AuthIdentityRepository authIdentityRepository) {
        this.userRepository = userRepository;
        this.authIdentityRepository = authIdentityRepository;
    }

    @Override
    public User createGuest(String displayName) {
        String normalizedDisplayName = normalizeDisplayName(displayName);
        User guest = userRepository.save(new User(UserId.newId(), UserType.GUEST, normalizedDisplayName));
        authIdentityRepository.save(new AuthIdentity(
                null,
                guest.userId(),
                AuthProvider.GUEST,
                guest.userId().value(),
                null
        ));
        return guest;
    }

    @Override
    public User get(UserId userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private String normalizeDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return DEFAULT_GUEST_DISPLAY_NAME;
        }
        return displayName.trim();
    }
}
