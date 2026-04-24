package io.wowtalk.message.dto;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;

public record SendChatMessageCommand(
        RoomId roomId,
        SessionId sessionId,
        String payload
) {
}
