package io.wowtalk.websocket.transport;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;
import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.user.domain.UserId;
import io.wowtalk.user.service.UserService;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class WebSocketConnectionResolver {

    static final String ROOM_ID = "roomId";
    static final String CONNECTION_ID = "connectionId";
    static final String SESSION_ID = "sessionId";
    static final String USER_ID = "userId";
    static final String PROTOCOL_VERSION = "protocolVersion";

    private final UserService userService;

    public WebSocketConnectionResolver(UserService userService) {
        this.userService = userService;
    }

    WebSocketConnectionInfo resolve(URI uri) {
        if (uri == null || uri.getQuery() == null || uri.getQuery().isBlank()) {
            throw new WowTalkException(ErrorCode.WEBSOCKET_CONNECTION_INVALID);
        }

        Map<String, String> queryParams = parseQueryParams(uri.getQuery());
        String roomId = queryParams.get(ROOM_ID);
        String sessionId = queryParams.get(SESSION_ID);

        if (roomId == null || roomId.isBlank() || sessionId == null || sessionId.isBlank()) {
            throw new WowTalkException(ErrorCode.WEBSOCKET_CONNECTION_INVALID);
        }

        return new WebSocketConnectionInfo(
                new RoomId(roomId),
                resolveConnectionId(queryParams.get(CONNECTION_ID)),
                new SessionId(sessionId),
                resolveUserId(queryParams.get(USER_ID), sessionId),
                resolveProtocolVersion(queryParams.get(PROTOCOL_VERSION))
        );
    }

    private ConnectionId resolveConnectionId(String connectionId) {
        if (connectionId == null || connectionId.isBlank()) {
            return ConnectionId.newId();
        }
        return new ConnectionId(connectionId);
    }

    private UserId resolveUserId(String userId, String legacySessionId) {
        if (userId == null || userId.isBlank()) {
            // 기존 클라이언트는 sessionId를 발신자처럼 사용했으므로, 프론트가 userId로 완전히 전환될 때까지 보존한다.
            return userService.createGuest(legacySessionId).userId();
        }
        UserId resolvedUserId = new UserId(userId);
        userService.get(resolvedUserId);
        return resolvedUserId;
    }

    private String resolveProtocolVersion(String protocolVersion) {
        if (protocolVersion == null || protocolVersion.isBlank()) {
            return WebSocketProtocol.LEGACY_VERSION;
        }
        if (!WebSocketProtocol.VERSION_1.equals(protocolVersion)) {
            throw new WowTalkException(ErrorCode.WEBSOCKET_CONNECTION_INVALID);
        }
        return protocolVersion;
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();

        for (String part : query.split("&")) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                params.put(decode(keyValue[0]), decode(keyValue[1]));
            }
        }

        return params;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
