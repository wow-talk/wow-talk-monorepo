package io.wowtalk.websocket.transport;

import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketSessionRegistry {

    private final Map<RoomId, Map<ConnectionId, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, SessionRegistration> registrationsByConnectionId = new ConcurrentHashMap<>();

    public void register(RoomId roomId, ConnectionId connectionId, SessionId sessionId, WebSocketSession webSocketSession) {
        roomSessions.computeIfAbsent(roomId, key -> new ConcurrentHashMap<>())
                .put(connectionId, webSocketSession);
        registrationsByConnectionId.put(webSocketSession.getId(), new SessionRegistration(roomId, connectionId, sessionId));
    }

    public void unregister(WebSocketSession webSocketSession) {
        SessionRegistration registration = registrationsByConnectionId.remove(webSocketSession.getId());
        if (registration == null) {
            return;
        }

        Map<ConnectionId, WebSocketSession> sessions = roomSessions.get(registration.roomId());
        if (sessions == null) {
            return;
        }

        sessions.remove(registration.connectionId());
        if (sessions.isEmpty()) {
            roomSessions.remove(registration.roomId());
        }
    }

    public Map<ConnectionId, WebSocketSession> getRoomSessions(RoomId roomId) {
        return roomSessions.getOrDefault(roomId, Map.of());
    }

    private record SessionRegistration(
            RoomId roomId,
            ConnectionId connectionId,
            SessionId sessionId
    ) {
    }
}
