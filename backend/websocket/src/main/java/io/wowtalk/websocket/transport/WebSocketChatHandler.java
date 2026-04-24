package io.wowtalk.websocket.transport;

import io.wowtalk.channel.service.ChannelService;
import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;
import io.wowtalk.message.dto.ChatMessageResult;
import io.wowtalk.message.dto.SendChatMessageCommand;
import io.wowtalk.message.service.ChatService;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.transport.TransportRouter;
import io.wowtalk.websocket.error.InvalidWebSocketMessageFormatException;
import io.wowtalk.websocket.error.UnsupportedWebSocketMessageTypeException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

@Component
public class WebSocketChatHandler extends TextWebSocketHandler {

    private static final String ROOM_ID = "roomId";
    private static final String SESSION_ID = "sessionId";

    private final ChannelService channelService;
    private final ChatService chatService;
    private final TransportRouter transportRouter;
    private final WebSocketSessionRegistry sessionRegistry;
    private final WebSocketChatTransport webSocketChatTransport;
    private final ObjectMapper objectMapper;

    public WebSocketChatHandler(
            ChannelService channelService,
            ChatService chatService,
            TransportRouter transportRouter,
            WebSocketSessionRegistry sessionRegistry,
            WebSocketChatTransport webSocketChatTransport,
            ObjectMapper objectMapper
    ) {
        this.channelService = channelService;
        this.chatService = chatService;
        this.transportRouter = transportRouter;
        this.sessionRegistry = sessionRegistry;
        this.webSocketChatTransport = webSocketChatTransport;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            ConnectionInfo connectionInfo = resolveConnectionInfo(session.getUri());

            channelService.ensureChannel(connectionInfo.roomId(), TransportMode.WEBSOCKET);
            sessionRegistry.register(connectionInfo.roomId(), connectionInfo.sessionId(), session);
            session.getAttributes().put(ROOM_ID, connectionInfo.roomId());
            session.getAttributes().put(SESSION_ID, connectionInfo.sessionId());
            webSocketChatTransport.sendSystemMessage(
                    session,
                    WebSocketOutboundMessage.connected(connectionInfo.roomId().value(), connectionInfo.sessionId().value())
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
            SessionId sessionId = (SessionId) session.getAttributes().get(SESSION_ID);
            WebSocketInboundMessage inboundMessage = parseInboundMessage(message.getPayload());

            if (inboundMessage.type() != WebSocketMessageType.SEND_MESSAGE) {
                throw new UnsupportedWebSocketMessageTypeException();
            }

            ChatMessageResult result = chatService.send(new SendChatMessageCommand(
                    roomId,
                    sessionId,
                    inboundMessage.payload()
            ));

            transportRouter.route(TransportMode.WEBSOCKET).broadcast(
                    result.roomId(),
                    new TransportMessage(result.roomId(), result.sessionId(), result.payload(), result.sentAt())
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
        String sessionId = queryParams.get(SESSION_ID);

        if (roomId == null || roomId.isBlank() || sessionId == null || sessionId.isBlank()) {
            throw new WowTalkException(ErrorCode.WEBSOCKET_CONNECTION_INVALID);
        }

        return new ConnectionInfo(new RoomId(roomId), new SessionId(sessionId));
    }

    private WebSocketInboundMessage parseInboundMessage(String payload) {
        try {
            WebSocketInboundMessage inboundMessage = objectMapper.readValue(payload, WebSocketInboundMessage.class);
            if (inboundMessage.type() == null) {
                throw new InvalidWebSocketMessageFormatException();
            }
            return inboundMessage;
        } catch (WowTalkException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidWebSocketMessageFormatException();
        }
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
            SessionId sessionId
    ) {
    }
}
