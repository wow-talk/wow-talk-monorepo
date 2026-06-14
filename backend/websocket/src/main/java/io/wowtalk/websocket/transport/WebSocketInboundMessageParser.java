package io.wowtalk.websocket.transport;

import io.wowtalk.common.error.WowTalkException;
import io.wowtalk.websocket.error.InvalidWebSocketMessageFormatException;
import io.wowtalk.websocket.error.UnsupportedWebSocketMessageTypeException;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * WebSocket inbound JSON을 내부 채팅 command에 가까운 ParsedInboundChatMessage로 변환한다.
 *
 * <p>legacy SEND_MESSAGE와 protocol v1 CHAT_SEND를 함께 해석한다. 파싱 실패는 도메인 예외가 아니라
 * WebSocket adapter 입력 계약 위반으로 다룬다.
 */
@Component
public class WebSocketInboundMessageParser {

    private final ObjectMapper objectMapper;

    public WebSocketInboundMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public ParsedInboundChatMessage parseChatMessage(String payload) {
        try {
            Map<String, Object> message = objectMapper.readValue(payload, Map.class);
            Object type = message.get("type");
            if (!(type instanceof String typeName)) {
                throw new InvalidWebSocketMessageFormatException();
            }
            Object version = message.get("version");
            if (version == null) {
                return parseLegacy(typeName, message);
            }
            return parseEnvelope(typeName, version, message);
        } catch (WowTalkException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidWebSocketMessageFormatException();
        }
    }

    private ParsedInboundChatMessage parseLegacy(String typeName, Map<String, Object> message) {
        if (WebSocketMessageType.valueOf(typeName) != WebSocketMessageType.SEND_MESSAGE) {
            throw new UnsupportedWebSocketMessageTypeException();
        }
        Object payload = message.get("payload");
        if (!(payload instanceof String text)) {
            throw new InvalidWebSocketMessageFormatException();
        }
        return new ParsedInboundChatMessage(null, text);
    }

    private ParsedInboundChatMessage parseEnvelope(String typeName, Object version, Map<String, Object> message) {
        if (!(version instanceof Number protocolVersion) || protocolVersion.intValue() != 1) {
            throw new UnsupportedWebSocketMessageTypeException();
        }
        if (WebSocketMessageType.valueOf(typeName) != WebSocketMessageType.CHAT_SEND) {
            throw new UnsupportedWebSocketMessageTypeException();
        }
        Object payload = message.get("payload");
        if (!(payload instanceof Map<?, ?> payloadMap)) {
            throw new InvalidWebSocketMessageFormatException();
        }

        Object text = payloadMap.get("text");
        if (!(text instanceof String messageText)) {
            throw new InvalidWebSocketMessageFormatException();
        }
        Object requestId = message.get("requestId");
        return new ParsedInboundChatMessage(requestId instanceof String value ? value : null, messageText);
    }
}
