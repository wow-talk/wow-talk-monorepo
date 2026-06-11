package io.wowtalk.redis.subscriber;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.redis.message.RedisRealtimeMessage;
import io.wowtalk.transport.ChatTransport;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.transport.TransportRouter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.DefaultMessage;
import tools.jackson.databind.ObjectMapper;

class RedisRealtimeEventSubscriberTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void redis_message를_로컬_websocket_transport로_broadcast한다() throws Exception {
        StubTransport transport = new StubTransport();
        RedisRealtimeEventSubscriber subscriber = new RedisRealtimeEventSubscriber(
                objectMapper,
                new StubTransportRouter(transport)
        );
        RedisRealtimeMessage message = new RedisRealtimeMessage(
                "message-1",
                "lobby",
                "connection-1",
                "session-1",
                "user-1",
                "hello",
                "2026-06-11T10:00:00Z",
                "request-1"
        );

        subscriber.onMessage(new DefaultMessage(
                "wowtalk:room-events:lobby".getBytes(StandardCharsets.UTF_8),
                objectMapper.writeValueAsString(message).getBytes(StandardCharsets.UTF_8)
        ), null);

        assertThat(transport.roomId).isEqualTo(new RoomId("lobby"));
        assertThat(transport.message.messageId()).isEqualTo("message-1");
        assertThat(transport.message.requestId()).isEqualTo("request-1");
    }

    private record StubTransportRouter(ChatTransport transport) implements TransportRouter {

        @Override
        public ChatTransport route(TransportMode transportMode) {
            assertThat(transportMode).isEqualTo(TransportMode.WEBSOCKET);
            return transport;
        }
    }

    private static class StubTransport implements ChatTransport {

        private RoomId roomId;
        private TransportMessage message;

        @Override
        public TransportMode mode() {
            return TransportMode.WEBSOCKET;
        }

        @Override
        public void sendToSession(TransportMessage message) {
        }

        @Override
        public void broadcast(RoomId roomId, TransportMessage message) {
            this.roomId = roomId;
            this.message = message;
        }
    }
}
