package io.wowtalk.message.repository;

import io.wowtalk.message.domain.ChatMessage;
import io.wowtalk.transport.RoomId;
import java.util.List;

public interface ChatMessageRepository {

    ChatMessage save(ChatMessage chatMessage);

    List<ChatMessage> findAll();

    List<ChatMessage> findRecentByRoomId(RoomId roomId, int limit);
}
