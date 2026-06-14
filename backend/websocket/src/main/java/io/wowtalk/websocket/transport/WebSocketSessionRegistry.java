package io.wowtalk.websocket.transport;

import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * 현재 API task에 붙어 있는 WebSocket 연결만 관리하는 local registry다.
 *
 * <p>이 registry는 전체 방 presence나 global session store가 아니다. ECS/Fargate에서 API task가
 * 여러 개로 늘어나면 서버 간 전파는 Redis Pub/Sub 같은 realtime broker가 담당하고, 각 task는
 * 자기 JVM에 붙은 socket에만 broadcast한다.
 */
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
