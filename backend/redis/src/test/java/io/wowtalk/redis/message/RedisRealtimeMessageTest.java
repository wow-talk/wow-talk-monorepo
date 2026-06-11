package io.wowtalk.redis.message;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.transport.TransportMessage;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RedisRealtimeMessageTest {

    @Test
    void transportMessage를_redis_message로_변환하고_복원한다() {
        TransportMessage transportMessage = new TransportMessage(
                "message-1",
                new RoomId("lobby"),
                new ConnectionId("connection-1"),
                new SessionId("session-1"),
                "user-1",
                "hello",
                Instant.parse("2026-06-11T10:00:00Z"),
                "request-1"
        );

        TransportMessage restored = RedisRealtimeMessage.from(transportMessage)
                .toTransportMessage();

        assertThat(restored).isEqualTo(transportMessage);
    }
}
