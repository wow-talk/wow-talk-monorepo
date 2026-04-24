package io.wowtalk.websocket.transport;

public record WebSocketInboundMessage(
        WebSocketMessageType type,
        String payload
) {
}
