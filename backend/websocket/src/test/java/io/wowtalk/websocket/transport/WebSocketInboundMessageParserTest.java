package io.wowtalk.websocket.transport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import io.wowtalk.websocket.error.InvalidWebSocketMessageFormatException;
import io.wowtalk.websocket.error.UnsupportedWebSocketMessageTypeException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class WebSocketInboundMessageParserTest {

    private final WebSocketInboundMessageParser parser = new WebSocketInboundMessageParser(new ObjectMapper());

    @Test
    void legacy_SEND_MESSAGE를_채팅_명령으로_파싱한다() {
        ParsedInboundChatMessage message = parser.parseChatMessage("""
                {
                  "type": "SEND_MESSAGE",
                  "payload": "안녕하세요"
                }
                """);

        assertThat(message.requestId()).isNull();
        assertThat(message.roomId()).isNull();
        assertThat(message.payload()).isEqualTo("안녕하세요");
    }

    @Test
    void v1_CHAT_SEND를_채팅_명령으로_파싱한다() {
        ParsedInboundChatMessage message = parser.parseChatMessage("""
                {
                  "version": 1,
                  "type": "CHAT_SEND",
                  "requestId": "req-1",
                  "roomId": "lobby",
                  "payload": {
                    "text": "안녕하세요"
                  }
                }
                """);

        assertThat(message.requestId()).isEqualTo("req-1");
        assertThat(message.roomId()).isEqualTo("lobby");
        assertThat(message.payload()).isEqualTo("안녕하세요");
    }

    @Test
    void 지원하지_않는_v1_타입은_예외가_발생한다() {
        UnsupportedWebSocketMessageTypeException exception = catchThrowableOfType(
                UnsupportedWebSocketMessageTypeException.class,
                () -> parser.parseChatMessage("""
                {
                  "version": 1,
                  "type": "PING",
                  "requestId": "req-1",
                  "roomId": "lobby",
                  "payload": {}
                }
                """)
        );

        assertThat(exception.requestId()).isEqualTo("req-1");
        assertThat(exception.roomId()).isEqualTo("lobby");
    }

    @Test
    void v1_채팅_payload에_text가_없으면_예외가_발생한다() {
        InvalidWebSocketMessageFormatException exception = catchThrowableOfType(
                InvalidWebSocketMessageFormatException.class,
                () -> parser.parseChatMessage("""
                {
                  "version": 1,
                  "type": "CHAT_SEND",
                  "requestId": "req-1",
                  "roomId": "lobby",
                  "payload": {}
                }
                """)
        );

        assertThat(exception.requestId()).isEqualTo("req-1");
        assertThat(exception.roomId()).isEqualTo("lobby");
    }
}
