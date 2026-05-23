package io.wowtalk.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.user.domain.User;
import io.wowtalk.user.domain.UserType;
import io.wowtalk.user.repository.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

class DefaultUserServiceTest {

    private final DefaultUserService userService = new DefaultUserService(new InMemoryUserRepository());

    @Test
    void 게스트_사용자를_생성한다() {
        User guest = userService.createGuest("player");

        assertThat(guest.userId()).isNotNull();
        assertThat(guest.userType()).isEqualTo(UserType.GUEST);
        assertThat(guest.displayName()).isEqualTo("player");
    }

    @Test
    void 표시_이름이_없으면_기본_게스트_이름을_사용한다() {
        User guest = userService.createGuest(" ");

        assertThat(guest.displayName()).isEqualTo("guest");
    }
}
