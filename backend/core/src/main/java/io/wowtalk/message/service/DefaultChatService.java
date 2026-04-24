package io.wowtalk.message.service;

import io.wowtalk.channel.dto.ChannelTransportInfo;
import io.wowtalk.channel.service.ChannelService;
import io.wowtalk.message.domain.ChatMessage;
import io.wowtalk.message.dto.ChatMessageHistoryResponse;
import io.wowtalk.message.dto.ChatMessageResult;
import io.wowtalk.message.dto.SendChatMessageCommand;
import io.wowtalk.message.repository.ChatMessageRepository;
import io.wowtalk.transport.RoomId;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DefaultChatService implements ChatService {

    private final ChannelService channelService;
    private final ChatMessageRepository chatMessageRepository;

    public DefaultChatService(ChannelService channelService, ChatMessageRepository chatMessageRepository) {
        this.channelService = channelService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ChatMessageResult send(SendChatMessageCommand command) {
        validatePayload(command.payload());

        ChannelTransportInfo channelTransportInfo = channelService.getTransportInfo(command.roomId());
        Instant sentAt = Instant.now();

        ChatMessage chatMessage = new ChatMessage(
                channelTransportInfo.roomId(),
                command.sessionId(),
                command.payload(),
                sentAt
        );

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        return new ChatMessageResult(
                savedMessage.roomId(),
                savedMessage.sessionId(),
                savedMessage.payload(),
                savedMessage.sentAt()
        );
    }

    @Override
    public List<ChatMessageResult> getRecentMessages(RoomId roomId, int limit) {
        channelService.getTransportInfo(roomId);

        return chatMessageRepository.findRecentByRoomId(roomId, limit).stream()
                .map(message -> new ChatMessageResult(
                        message.roomId(),
                        message.sessionId(),
                        message.payload(),
                        message.sentAt()
                ))
                .toList();
    }

    private void validatePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new InvalidChatMessageException();
        }
    }
}
