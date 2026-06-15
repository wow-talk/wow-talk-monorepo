package io.wowtalk.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.user.domain.AuthProvider;
import io.wowtalk.user.domain.User;
import io.wowtalk.user.domain.UserType;
import io.wowtalk.user.repository.InMemoryAuthIdentityRepository;
import io.wowtalk.user.repository.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

class DefaultUserServiceTest {

    private final InMemoryAuthIdentityRepository authIdentityRepository = new InMemoryAuthIdentityRepository();
    private final DefaultUserService userService =
            new DefaultUserService(new InMemoryUserRepository(), authIdentityRepository);

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

    @Test
    void 게스트_사용자를_생성할_때_인증_identity도_함께_저장한다() {
        User guest = userService.createGuest("player");

        assertThat(authIdentityRepository.findByProviderAndProviderSubject(AuthProvider.GUEST, guest.userId().value()))
                .hasValueSatisfying(identity -> {
                    assertThat(identity.userId()).isEqualTo(guest.userId());
                    assertThat(identity.provider()).isEqualTo(AuthProvider.GUEST);
                    assertThat(identity.providerSubject()).isEqualTo(guest.userId().value());
                });
    }
}
