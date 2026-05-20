package io.wowtalk.message.service;

import io.wowtalk.message.dto.ChatMessageResult;
import io.wowtalk.message.dto.SendChatMessageCommand;
import io.wowtalk.transport.RoomId;
import java.util.List;

public interface ChatService {

    ChatMessageResult send(SendChatMessageCommand command);

    List<ChatMessageResult> getRecentMessages(RoomId roomId, int limit);
}
