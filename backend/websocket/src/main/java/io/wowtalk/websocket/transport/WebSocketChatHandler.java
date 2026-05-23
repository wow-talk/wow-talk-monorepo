package io.wowtalk.websocket.transport;

import io.wowtalk.channel.service.ChannelService;
import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;
import io.wowtalk.message.dto.ChatMessageResult;
import io.wowtalk.message.dto.SendChatMessageCommand;
import io.wowtalk.message.service.ChatService;
import io.wowtalk.room.service.RoomMemberService;
import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.transport.TransportRouter;
import io.wowtalk.user.domain.UserId;
import io.wowtalk.user.service.UserService;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketChatHandler extends TextWebSocketHandler {

    private static final String ROOM_ID = "roomId";
    private static final String CONNECTION_ID = "connectionId";
    private static final String SESSION_ID = "sessionId";
    private static final String USER_ID = "userId";

    private final ChannelService channelService;
    private final ChatService chatService;
    private final TransportRouter transportRouter;
    private final UserService userService;
    private final RoomMemberService roomMemberService;
    private final WebSocketSessionRegistry sessionRegistry;
    private final WebSocketChatTransport webSocketChatTransport;
    private final WebSocketInboundMessageParser inboundMessageParser;

    public WebSocketChatHandler(
            ChannelService channelService,
            ChatService chatService,
            TransportRouter transportRouter,
            UserService userService,
            RoomMemberService roomMemberService,
            WebSocketSessionRegistry sessionRegistry,
            WebSocketChatTransport webSocketChatTransport,
            WebSocketInboundMessageParser inboundMessageParser
    ) {
        this.channelService = channelService;
        this.chatService = chatService;
        this.transportRouter = transportRouter;
        this.userService = userService;
        this.roomMemberService = roomMemberService;
        this.sessionRegistry = sessionRegistry;
        this.webSocketChatTransport = webSocketChatTransport;
        this.inboundMessageParser = inboundMessageParser;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            ConnectionInfo connectionInfo = resolveConnectionInfo(session.getUri());

            channelService.ensureChannel(connectionInfo.roomId(), TransportMode.WEBSOCKET);
            roomMemberService.join(connectionInfo.roomId(), connectionInfo.userId());
            sessionRegistry.register(connectionInfo.roomId(), connectionInfo.connectionId(), connectionInfo.sessionId(), session);
            session.getAttributes().put(ROOM_ID, connectionInfo.roomId());
            session.getAttributes().put(CONNECTION_ID, connectionInfo.connectionId());
            session.getAttributes().put(SESSION_ID, connectionInfo.sessionId());
            session.getAttributes().put(USER_ID, connectionInfo.userId());
            webSocketChatTransport.sendSystemMessage(
                    session,
                    WebSocketOutboundMessage.connected(
                            connectionInfo.roomId().value(),
                            connectionInfo.connectionId().value(),
                            connectionInfo.sessionId().value()
                    )
            );
        } catch (WowTalkException exception) {
            sendError(session, exception.errorCode());
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            RoomId roomId = (RoomId) session.getAttributes().get(ROOM_ID);
            ConnectionId connectionId = (ConnectionId) session.getAttributes().get(CONNECTION_ID);
            SessionId sessionId = (SessionId) session.getAttributes().get(SESSION_ID);
            UserId userId = (UserId) session.getAttributes().get(USER_ID);
            ParsedInboundChatMessage inboundMessage = inboundMessageParser.parseChatMessage(message.getPayload());

            ChatMessageResult result = chatService.send(new SendChatMessageCommand(
                    roomId,
                    sessionId,
                    userId,
                    inboundMessage.payload()
            ));

            transportRouter.route(TransportMode.WEBSOCKET).broadcast(
                    result.roomId(),
                    new TransportMessage(
                            result.messageId().value(),
                            result.roomId(),
                            connectionId,
                            result.sessionId(),
                            result.senderUserId().value(),
                            result.payload(),
                            result.sentAt()
                    ).withRequestId(inboundMessage.requestId())
            );
        } catch (WowTalkException exception) {
            sendError(session, exception.errorCode());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.unregister(session);
    }

    private ConnectionInfo resolveConnectionInfo(URI uri) {
        if (uri == null || uri.getQuery() == null || uri.getQuery().isBlank()) {
            throw new WowTalkException(ErrorCode.WEBSOCKET_CONNECTION_INVALID);
        }

        Map<String, String> queryParams = parseQueryParams(uri.getQuery());
        String roomId = queryParams.get(ROOM_ID);
        String connectionId = queryParams.get(CONNECTION_ID);
        String sessionId = queryParams.get(SESSION_ID);
        String userId = queryParams.get(USER_ID);

        if (roomId == null || roomId.isBlank() || sessionId == null || sessionId.isBlank()) {
            throw new WowTalkException(ErrorCode.WEBSOCKET_CONNECTION_INVALID);
        }

        UserId resolvedUserId = resolveUserId(userId, sessionId);

        ConnectionId resolvedConnectionId = resolveConnectionId(connectionId);

        return new ConnectionInfo(new RoomId(roomId), resolvedConnectionId, new SessionId(sessionId), resolvedUserId);
    }

    private ConnectionId resolveConnectionId(String connectionId) {
        if (connectionId == null || connectionId.isBlank()) {
            return ConnectionId.newId();
        }
        return new ConnectionId(connectionId);
    }

    private UserId resolveUserId(String userId, String legacySessionId) {
        if (userId == null || userId.isBlank()) {
            return userService.createGuest(legacySessionId).userId();
        }
        UserId resolvedUserId = new UserId(userId);
        userService.get(resolvedUserId);
        return resolvedUserId;
    }

    private void sendError(WebSocketSession session, ErrorCode errorCode) {
        if (session.isOpen()) {
            webSocketChatTransport.sendSystemMessage(session, WebSocketOutboundMessage.error(errorCode));
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();

        for (String part : query.split("&")) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }

        return params;
    }

    private record ConnectionInfo(
            RoomId roomId,
            ConnectionId connectionId,
            SessionId sessionId,
            UserId userId
    ) {
    }
}
