package io.wowtalk.websocket.transport;

import io.wowtalk.channel.service.ChannelService;
import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;
import io.wowtalk.message.dto.ChatMessageResult;
import io.wowtalk.message.dto.SendChatMessageCommand;
import io.wowtalk.message.service.ChatService;
import io.wowtalk.room.service.RoomMemberService;
import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RealtimeEventPublisher;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.user.domain.UserId;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketChatHandler extends TextWebSocketHandler {

    private static final String ROOM_ID_ATTRIBUTE = "roomId";
    private static final String CONNECTION_ID_ATTRIBUTE = "connectionId";
    private static final String SESSION_ID_ATTRIBUTE = "sessionId";
    private static final String USER_ID_ATTRIBUTE = "userId";

    private final ChannelService channelService;
    private final ChatService chatService;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final RoomMemberService roomMemberService;
    private final WebSocketSessionRegistry sessionRegistry;
    private final WebSocketChatTransport webSocketChatTransport;
    private final WebSocketInboundMessageParser inboundMessageParser;
    private final WebSocketConnectionResolver connectionResolver;

    public WebSocketChatHandler(
            ChannelService channelService,
            ChatService chatService,
            RealtimeEventPublisher realtimeEventPublisher,
            RoomMemberService roomMemberService,
            WebSocketSessionRegistry sessionRegistry,
            WebSocketChatTransport webSocketChatTransport,
            WebSocketInboundMessageParser inboundMessageParser,
            WebSocketConnectionResolver connectionResolver
    ) {
        this.channelService = channelService;
        this.chatService = chatService;
        this.realtimeEventPublisher = realtimeEventPublisher;
        this.roomMemberService = roomMemberService;
        this.sessionRegistry = sessionRegistry;
        this.webSocketChatTransport = webSocketChatTransport;
        this.inboundMessageParser = inboundMessageParser;
        this.connectionResolver = connectionResolver;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            WebSocketConnectionInfo connectionInfo = connectionResolver.resolve(session.getUri());

            // 세션 registry는 현재 API task의 local socket만 관리하고, 서버 간 fan-out은 RealtimeEventPublisher가 담당한다.
            channelService.ensureChannel(connectionInfo.roomId(), TransportMode.WEBSOCKET);
            roomMemberService.join(connectionInfo.roomId(), connectionInfo.userId());
            registerConnection(session, connectionInfo);
            webSocketChatTransport.sendSystemMessage(session, connectedMessage(connectionInfo));
        } catch (WowTalkException exception) {
            sendError(session, exception.errorCode());
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            RoomId roomId = (RoomId) session.getAttributes().get(ROOM_ID_ATTRIBUTE);
            ConnectionId connectionId = (ConnectionId) session.getAttributes().get(CONNECTION_ID_ATTRIBUTE);
            SessionId sessionId = (SessionId) session.getAttributes().get(SESSION_ID_ATTRIBUTE);
            UserId userId = (UserId) session.getAttributes().get(USER_ID_ATTRIBUTE);
            ParsedInboundChatMessage inboundMessage = inboundMessageParser.parseChatMessage(message.getPayload());

            ChatMessageResult result = chatService.send(new SendChatMessageCommand(
                    roomId,
                    sessionId,
                    userId,
                    inboundMessage.payload()
            ));

            realtimeEventPublisher.publish(
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

    private void registerConnection(WebSocketSession session, WebSocketConnectionInfo connectionInfo) {
        sessionRegistry.register(connectionInfo.roomId(), connectionInfo.connectionId(), connectionInfo.sessionId(), session);
        session.getAttributes().put(ROOM_ID_ATTRIBUTE, connectionInfo.roomId());
        session.getAttributes().put(CONNECTION_ID_ATTRIBUTE, connectionInfo.connectionId());
        session.getAttributes().put(SESSION_ID_ATTRIBUTE, connectionInfo.sessionId());
        session.getAttributes().put(USER_ID_ATTRIBUTE, connectionInfo.userId());
        session.getAttributes().put(WebSocketProtocol.PROTOCOL_VERSION_ATTRIBUTE, connectionInfo.protocolVersion());
    }

    private Object connectedMessage(WebSocketConnectionInfo connectionInfo) {
        if (connectionInfo.usesProtocolV1()) {
            return RealtimeOutboundMessage.connected(
                    connectionInfo.roomId().value(),
                    connectionInfo.connectionId().value(),
                    connectionInfo.sessionId().value(),
                    connectionInfo.userId().value()
            );
        }
        return WebSocketOutboundMessage.connected(
                connectionInfo.roomId().value(),
                connectionInfo.connectionId().value(),
                connectionInfo.sessionId().value()
        );
    }

    private void sendError(WebSocketSession session, ErrorCode errorCode) {
        if (session.isOpen()) {
            if (webSocketChatTransport.usesProtocolV1(session)) {
                webSocketChatTransport.sendSystemMessage(session, RealtimeOutboundMessage.error(errorCode));
                return;
            }
            webSocketChatTransport.sendSystemMessage(session, WebSocketOutboundMessage.error(errorCode));
        }
    }
}
