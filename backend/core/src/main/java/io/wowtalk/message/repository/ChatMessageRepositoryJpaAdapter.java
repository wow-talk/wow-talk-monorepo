package io.wowtalk.message.repository;

import io.wowtalk.message.domain.ChatMessage;
import io.wowtalk.message.domain.ChatMessageEntity;
import io.wowtalk.message.domain.MessageId;
import io.wowtalk.message.domain.MessageStatus;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import java.util.Comparator;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Profile("!test")
@Primary
@Repository
public class ChatMessageRepositoryJpaAdapter implements ChatMessageRepository {

    private final ChatMessageJpaRepository chatMessageJpaRepository;

    public ChatMessageRepositoryJpaAdapter(ChatMessageJpaRepository chatMessageJpaRepository) {
        this.chatMessageJpaRepository = chatMessageJpaRepository;
    }

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        ChatMessageEntity saved = chatMessageJpaRepository.save(new ChatMessageEntity(
                chatMessage.messageId().value(),
                chatMessage.roomId().value(),
                chatMessage.sessionId().value(),
                chatMessage.payload(),
                MessageStatus.ACTIVE,
                chatMessage.sentAt()
        ));
        return toDomain(saved);
    }

    @Override
    public List<ChatMessage> findAll() {
        return chatMessageJpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ChatMessage> findRecentByRoomId(RoomId roomId, int limit) {
        return chatMessageJpaRepository.findByRoomIdOrderBySentAtDesc(roomId.value(), PageRequest.of(0, limit)).stream()
                .map(this::toDomain)
                .sorted(Comparator.comparing(ChatMessage::sentAt))
                .toList();
    }

    private ChatMessage toDomain(ChatMessageEntity entity) {
        return new ChatMessage(
                new MessageId(entity.getMessageId()),
                new RoomId(entity.getRoomId()),
                new SessionId(entity.getSessionId()),
                entity.getPayload(),
                entity.getSentAt()
        );
    }
}
