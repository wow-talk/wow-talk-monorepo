package io.wowtalk.websocket.transport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.wowtalk.common.error.WowTalkException;
import io.wowtalk.user.domain.User;
import io.wowtalk.user.repository.InMemoryAuthIdentityRepository;
import io.wowtalk.user.repository.InMemoryUserRepository;
import io.wowtalk.user.service.DefaultUserService;
import java.net.URI;
import org.junit.jupiter.api.Test;

class WebSocketConnectionResolverTest {

    private final DefaultUserService userService =
            new DefaultUserService(new InMemoryUserRepository(), new InMemoryAuthIdentityRepository());
    private final WebSocketConnectionResolver resolver = new WebSocketConnectionResolver(userService);

    @Test
    void legacy_연결은_sessionId를_게스트_사용자로_보존한다() {
        WebSocketConnectionInfo connectionInfo = resolver.resolve(URI.create(
                "ws://localhost:8080/ws/chat?roomId=lobby&sessionId=guest-1"
        ));

        assertThat(connectionInfo.roomId().value()).isEqualTo("lobby");
        assertThat(connectionInfo.sessionId().value()).isEqualTo("guest-1");
        assertThat(connectionInfo.userId().value()).isNotBlank();
        assertThat(connectionInfo.protocolVersion()).isEqualTo(WebSocketProtocol.LEGACY_VERSION);
    }

    @Test
    void v1_연결은_명시된_connectionId와_userId를_사용한다() {
        User user = userService.createGuest("나정균");

        WebSocketConnectionInfo connectionInfo = resolver.resolve(URI.create(
                "ws://localhost:8080/ws/chat?roomId=lobby&connectionId=conn-1&sessionId=sess-1"
                        + "&userId=" + user.userId().value()
                        + "&protocolVersion=1"
        ));

        assertThat(connectionInfo.connectionId().value()).isEqualTo("conn-1");
        assertThat(connectionInfo.sessionId().value()).isEqualTo("sess-1");
        assertThat(connectionInfo.userId()).isEqualTo(user.userId());
        assertThat(connectionInfo.usesProtocolV1()).isTrue();
    }

    @Test
    void query_param은_url_decode한다() {
        WebSocketConnectionInfo connectionInfo = resolver.resolve(URI.create(
                "ws://localhost:8080/ws/chat?roomId=room%201&sessionId=guest%201"
        ));

        assertThat(connectionInfo.roomId().value()).isEqualTo("room 1");
        assertThat(connectionInfo.sessionId().value()).isEqualTo("guest 1");
    }

    @Test
    void 지원하지_않는_protocolVersion이면_예외가_발생한다() {
        assertThatThrownBy(() -> resolver.resolve(URI.create(
                "ws://localhost:8080/ws/chat?roomId=lobby&sessionId=sess-1&protocolVersion=2"
        ))).isInstanceOf(WowTalkException.class);
    }
}
