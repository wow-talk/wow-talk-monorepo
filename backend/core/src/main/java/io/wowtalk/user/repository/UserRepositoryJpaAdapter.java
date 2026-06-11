package io.wowtalk.user.repository;

import io.wowtalk.user.domain.User;
import io.wowtalk.user.domain.UserEntity;
import io.wowtalk.user.domain.UserId;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Profile("postgres")
@Primary
@Repository
public class UserRepositoryJpaAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryJpaAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findByUserId(UserId userId) {
        return userJpaRepository.findByUserId(userId.value())
                .map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity saved = userJpaRepository.save(new UserEntity(
                user.userId().value(),
                user.userType(),
                user.displayName()
        ));
        return toDomain(saved);
    }

    private User toDomain(UserEntity entity) {
        return new User(
                new UserId(entity.getUserId()),
                entity.getUserType(),
                entity.getDisplayName()
        );
    }
}
