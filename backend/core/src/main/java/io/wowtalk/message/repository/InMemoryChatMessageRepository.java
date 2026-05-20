package io.wowtalk.message.repository;

import io.wowtalk.message.domain.ChatMessage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryChatMessageRepository implements ChatMessageRepository {

    private final CopyOnWriteArrayList<ChatMessage> messages = new CopyOnWriteArrayList<>();

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        messages.add(chatMessage);
        return chatMessage;
    }

    @Override
    public List<ChatMessage> findAll() {
        return List.copyOf(messages);
    }

    @Override
    public List<ChatMessage> findRecentByRoomId(io.wowtalk.transport.RoomId roomId, int limit) {
        return messages.stream()
                .filter(message -> message.roomId().equals(roomId))
                .sorted((left, right) -> left.sentAt().compareTo(right.sentAt()))
                .skip(Math.max(0, messages.stream().filter(message -> message.roomId().equals(roomId)).count() - limit))
                .toList();
    }
}
