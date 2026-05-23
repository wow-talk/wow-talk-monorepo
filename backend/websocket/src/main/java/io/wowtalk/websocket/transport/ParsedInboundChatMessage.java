package io.wowtalk.websocket.transport;

public record ParsedInboundChatMessage(
        String requestId,
        String payload
) {
}
