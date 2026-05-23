package io.wowtalk.websocket.transport;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.transport.TransportMessage;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RealtimeOutboundMessageTest {

    @Test
    void 채팅_메시지를_v1_이벤트로_변환한다() {
        TransportMessage message = new TransportMessage(
                "message-1",
                new RoomId("room-1"),
                new ConnectionId("connection-1"),
                new SessionId("session-1"),
                "user-1",
                "hello",
                Instant.parse("2026-05-24T00:00:00Z"),
                "req-1"
        );

        RealtimeOutboundMessage outboundMessage = RealtimeOutboundMessage.chatMessageCreated(message);

        assertThat(outboundMessage.version()).isEqualTo(1);
        assertThat(outboundMessage.type()).isEqualTo(WebSocketMessageType.CHAT_MESSAGE_CREATED.name());
        assertThat(outboundMessage.requestId()).isEqualTo("req-1");
        assertThat(outboundMessage.roomId()).isEqualTo("room-1");
        assertThat(outboundMessage.occurredAt()).isEqualTo("2026-05-24T00:00:00Z");
        assertThat(outboundMessage.payload())
                .containsEntry("messageId", "message-1")
                .containsEntry("senderUserId", "user-1")
                .containsEntry("text", "hello");
    }
}
