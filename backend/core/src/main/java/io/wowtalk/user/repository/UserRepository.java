package io.wowtalk.user.repository;

import io.wowtalk.user.domain.User;
import io.wowtalk.user.domain.UserId;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUserId(UserId userId);

    User save(User user);
}
