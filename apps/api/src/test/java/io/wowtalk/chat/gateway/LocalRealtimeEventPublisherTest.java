package io.wowtalk.chat.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.transport.ChatTransport;
import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.transport.TransportRouter;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LocalRealtimeEventPublisherTest {

    @Test
    void publish는_로컬_websocket_transport로_broadcast한다() {
        StubTransport transport = new StubTransport();
        LocalRealtimeEventPublisher publisher = new LocalRealtimeEventPublisher(new StubTransportRouter(transport));
        RoomId roomId = new RoomId("room-1");
        TransportMessage message = new TransportMessage(
                "message-1",
                roomId,
                new ConnectionId("connection-1"),
                new SessionId("session-1"),
                "user-1",
                "hello",
                Instant.parse("2026-05-30T10:00:00Z")
        );

        publisher.publish(roomId, message);

        assertThat(transport.broadcastRoomId).isEqualTo(roomId);
        assertThat(transport.broadcastMessage).isEqualTo(message);
    }

    private record StubTransportRouter(ChatTransport transport) implements TransportRouter {

        @Override
        public ChatTransport route(TransportMode transportMode) {
            assertThat(transportMode).isEqualTo(TransportMode.WEBSOCKET);
            return transport;
        }
    }

    private static class StubTransport implements ChatTransport {

        private RoomId broadcastRoomId;
        private TransportMessage broadcastMessage;

        @Override
        public TransportMode mode() {
            return TransportMode.WEBSOCKET;
        }

        @Override
        public void sendToSession(TransportMessage message) {
        }

        @Override
        public void broadcast(RoomId roomId, TransportMessage message) {
            this.broadcastRoomId = roomId;
            this.broadcastMessage = message;
        }
    }
}
