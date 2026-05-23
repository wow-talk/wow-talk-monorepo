package io.wowtalk.message.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.wowtalk.channel.repository.InMemoryChannelRepository;
import io.wowtalk.channel.service.DefaultChannelService;
import io.wowtalk.message.dto.ChatMessageResult;
import io.wowtalk.message.dto.SendChatMessageCommand;
import io.wowtalk.message.repository.InMemoryChatMessageRepository;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.transport.TransportMode;
import org.junit.jupiter.api.Test;

class DefaultChatServiceTest {

    private final DefaultChannelService channelService = new DefaultChannelService(new InMemoryChannelRepository());
    private final DefaultChatService chatService =
            new DefaultChatService(channelService, new InMemoryChatMessageRepository());

    @Test
    void 채팅_메시지를_저장하고_결과를_반환한다() {
        RoomId roomId = new RoomId("room-1");
        channelService.ensureChannel(roomId, TransportMode.WEBSOCKET);

        ChatMessageResult result = chatService.send(new SendChatMessageCommand(
                roomId,
                new SessionId("session-1"),
                "안녕하세요"
        ));

        assertThat(result.messageId()).isNotNull();
        assertThat(result.messageId().value()).isNotBlank();
        assertThat(result.roomId()).isEqualTo(roomId);
        assertThat(result.sessionId()).isEqualTo(new SessionId("session-1"));
        assertThat(result.payload()).isEqualTo("안녕하세요");
        assertThat(result.sentAt()).isNotNull();
    }

    @Test
    void 비어있는_메시지는_예외가_발생한다() {
        RoomId roomId = new RoomId("room-1");
        channelService.ensureChannel(roomId, TransportMode.WEBSOCKET);

        assertThatThrownBy(() -> chatService.send(new SendChatMessageCommand(
                roomId,
                new SessionId("session-1"),
                " "
        ))).isInstanceOf(InvalidChatMessageException.class)
                .hasMessage("메시지 내용이 올바르지 않습니다.");
    }
}
