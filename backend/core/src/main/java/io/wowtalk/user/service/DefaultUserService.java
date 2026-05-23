package io.wowtalk.user.service;

import io.wowtalk.user.domain.User;
import io.wowtalk.user.domain.UserId;
import io.wowtalk.user.domain.UserType;
import io.wowtalk.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultUserService implements UserService {

    private static final String DEFAULT_GUEST_DISPLAY_NAME = "guest";

    private final UserRepository userRepository;

    public DefaultUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createGuest(String displayName) {
        String normalizedDisplayName = normalizeDisplayName(displayName);
        return userRepository.save(new User(UserId.newId(), UserType.GUEST, normalizedDisplayName));
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
