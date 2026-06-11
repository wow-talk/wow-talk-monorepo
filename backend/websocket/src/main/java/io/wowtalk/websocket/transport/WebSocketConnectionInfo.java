package io.wowtalk.websocket.transport;

import io.wowtalk.transport.ConnectionId;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.user.domain.UserId;

record WebSocketConnectionInfo(
        RoomId roomId,
        ConnectionId connectionId,
        SessionId sessionId,
        UserId userId,
        String protocolVersion
) {

    boolean usesProtocolV1() {
        return WebSocketProtocol.VERSION_1.equals(protocolVersion);
    }
}
