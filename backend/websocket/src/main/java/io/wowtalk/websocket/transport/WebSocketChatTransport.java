package io.wowtalk.websocket.transport;

import io.wowtalk.transport.ChatTransport;
import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

/**
 * core의 TransportMessage를 WebSocket 클라이언트가 이해하는 JSON 메시지로 변환해 전송한다.
 *
 * <p>legacy 응답과 protocol v1 envelope를 동시에 지원해 프론트 전환 기간에도 기존 클라이언트를
 * 깨지 않도록 한다.
 */
@Component
public class WebSocketChatTransport implements ChatTransport {

    private final WebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public WebSocketChatTransport(WebSocketSessionRegistry sessionRegistry, ObjectMapper objectMapper) {
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public TransportMode mode() {
        return TransportMode.WEBSOCKET;
    }

    @Override
    public void sendToSession(TransportMessage message) {
        WebSocketSession session = sessionRegistry.getRoomSessions(message.roomId()).get(message.connectionId());
        if (session == null || !session.isOpen()) {
            return;
        }

        send(session, createChatMessage(session, message));
    }

    @Override
    public void broadcast(RoomId roomId, TransportMessage message) {
        Map<ConnectionId, WebSocketSession> sessions = sessionRegistry.getRoomSessions(roomId);
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                send(session, createChatMessage(session, message));
            }
        }
    }

    public void sendSystemMessage(WebSocketSession session, Object outboundMessage) {
        send(session, outboundMessage);
    }

    private Object createChatMessage(WebSocketSession session, TransportMessage message) {
        if (usesProtocolV1(session)) {
            return RealtimeOutboundMessage.chatMessageCreated(message);
        }
        return WebSocketOutboundMessage.chatMessage(message);
    }

    public boolean usesProtocolV1(WebSocketSession session) {
        return WebSocketProtocol.VERSION_1.equals(String.valueOf(session.getAttributes().get(WebSocketProtocol.PROTOCOL_VERSION_ATTRIBUTE)));
    }

    private void send(WebSocketSession session, Object outboundChatMessage) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(outboundChatMessage)));
        } catch (Exception exception) {
            throw new IllegalStateException("웹소켓 메시지 처리에 실패했습니다.", exception);
        }
    }
}
