package io.wowtalk.user.repository;

import io.wowtalk.user.domain.User;
import io.wowtalk.user.domain.UserId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

    private final Map<UserId, User> users = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findByUserId(UserId userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public User save(User user) {
        users.put(user.userId(), user);
        return user;
    }
}
