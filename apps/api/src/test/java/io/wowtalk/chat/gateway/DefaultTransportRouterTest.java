package io.wowtalk.chat.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.wowtalk.transport.ChatTransport;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultTransportRouterTest {

    @Test
    void transportMode로_구현체를_선택한다() {
        ChatTransport websocketTransport = new StubTransport(TransportMode.WEBSOCKET);
        DefaultTransportRouter router = new DefaultTransportRouter(List.of(websocketTransport));

        ChatTransport routed = router.route(TransportMode.WEBSOCKET);

        assertThat(routed).isSameAs(websocketTransport);
    }

    @Test
    void 등록되지_않은_mode면_예외가_발생한다() {
        DefaultTransportRouter router = new DefaultTransportRouter(List.of());

        assertThatThrownBy(() -> router.route(TransportMode.RAW_TCP))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("지원하지 않는 transport 모드입니다: RAW_TCP");
    }

    private record StubTransport(TransportMode mode) implements ChatTransport {

        @Override
        public void sendToSession(TransportMessage message) {
        }

        @Override
        public void broadcast(RoomId roomId, TransportMessage message) {
        }
    }
}
