package io.wowtalk.user.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.user.repository.InMemoryAuthIdentityRepository;
import io.wowtalk.user.repository.InMemoryUserRepository;
import io.wowtalk.user.service.DefaultUserService;
import org.junit.jupiter.api.Test;

class GuestControllerTest {

    private final GuestController controller = new GuestController(new DefaultUserService(
            new InMemoryUserRepository(),
            new InMemoryAuthIdentityRepository()
    ));

    @Test
    void 게스트를_생성하면_프론트_연결에_필요한_식별자를_반환한다() {
        GuestController.GuestResponse response = controller.create(new GuestController.CreateGuestRequest("정균"));

        assertThat(response.userId()).isNotBlank();
        assertThat(response.userType()).isEqualTo("GUEST");
        assertThat(response.displayName()).isEqualTo("정균");
        assertThat(response.connectionId()).isNotBlank();
        assertThat(response.sessionId()).isEqualTo(response.connectionId());
    }
}
