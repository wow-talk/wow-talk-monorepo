package io.wowtalk.transport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransportMessageTest {

    @Test
    void sentAt이_없으면_현재_시간이_설정된다() {
        TransportMessage message = new TransportMessage(
                "message-1",
                new RoomId("room-1"),
                new SessionId("session-1"),
                "user-1",
                "hello",
                null
        );

        assertThat(message.sentAt()).isNotNull();
    }

    @Test
    void payload가_비어있으면_예외가_발생한다() {
        assertThatThrownBy(() -> new TransportMessage(
                "message-1",
                new RoomId("room-1"),
                new SessionId("session-1"),
                "user-1",
                " ",
                Instant.now()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메시지 내용은 비어 있을 수 없습니다.");
    }
}
