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

        send(session, WebSocketOutboundMessage.chatMessage(message));
    }

    @Override
    public void broadcast(RoomId roomId, TransportMessage message) {
        Map<ConnectionId, WebSocketSession> sessions = sessionRegistry.getRoomSessions(roomId);
        WebSocketOutboundMessage outboundChatMessage = WebSocketOutboundMessage.chatMessage(message);

        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                send(session, outboundChatMessage);
            }
        }
    }

    public void sendSystemMessage(WebSocketSession session, WebSocketOutboundMessage outboundMessage) {
        send(session, outboundMessage);
    }

    private void send(WebSocketSession session, WebSocketOutboundMessage outboundChatMessage) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(outboundChatMessage)));
        } catch (Exception exception) {
            throw new IllegalStateException("웹소켓 메시지 처리에 실패했습니다.", exception);
        }
    }
}
