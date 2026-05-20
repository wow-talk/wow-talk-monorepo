package io.wowtalk.websocket.transport;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketSessionRegistry {

    private final Map<RoomId, Map<SessionId, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, SessionRegistration> registrationsByConnectionId = new ConcurrentHashMap<>();

    public void register(RoomId roomId, SessionId sessionId, WebSocketSession webSocketSession) {
        roomSessions.computeIfAbsent(roomId, key -> new ConcurrentHashMap<>())
                .put(sessionId, webSocketSession);
        registrationsByConnectionId.put(webSocketSession.getId(), new SessionRegistration(roomId, sessionId));
    }

    public void unregister(WebSocketSession webSocketSession) {
        SessionRegistration registration = registrationsByConnectionId.remove(webSocketSession.getId());
        if (registration == null) {
            return;
        }

        Map<SessionId, WebSocketSession> sessions = roomSessions.get(registration.roomId());
        if (sessions == null) {
            return;
        }

        sessions.remove(registration.sessionId());
        if (sessions.isEmpty()) {
            roomSessions.remove(registration.roomId());
        }
    }

    public Map<SessionId, WebSocketSession> getRoomSessions(RoomId roomId) {
        return roomSessions.getOrDefault(roomId, Map.of());
    }

    private record SessionRegistration(
            RoomId roomId,
            SessionId sessionId
    ) {
    }
}
