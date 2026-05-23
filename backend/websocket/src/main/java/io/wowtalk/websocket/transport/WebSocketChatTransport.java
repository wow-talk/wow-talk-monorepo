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

@Component
public class WebSocketChatTransport implements ChatTransport {

    public static final String PROTOCOL_VERSION_ATTRIBUTE = "protocolVersion";

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
        return "1".equals(String.valueOf(session.getAttributes().get(PROTOCOL_VERSION_ATTRIBUTE)));
    }

    private void send(WebSocketSession session, Object outboundChatMessage) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(outboundChatMessage)));
        } catch (Exception exception) {
            throw new IllegalStateException("웹소켓 메시지 처리에 실패했습니다.", exception);
        }
    }
}
