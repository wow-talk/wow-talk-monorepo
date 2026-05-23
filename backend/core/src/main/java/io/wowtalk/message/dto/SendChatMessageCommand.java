package io.wowtalk.message.dto;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.user.domain.UserId;

public record SendChatMessageCommand(
        RoomId roomId,
        SessionId sessionId,
        UserId senderUserId,
        String payload
) {
}
