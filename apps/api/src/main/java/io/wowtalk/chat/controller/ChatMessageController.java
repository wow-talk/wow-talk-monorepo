package io.wowtalk.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wowtalk.message.dto.ChatMessageResult;
import io.wowtalk.message.service.ChatService;
import io.wowtalk.transport.RoomId;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Message", description = "메시지 조회 API")
@RestController
@RequestMapping("/api/v1/channels/{roomId}/messages")
public class ChatMessageController {

    private final ChatService chatService;

    public ChatMessageController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "최근 메시지 조회", description = "채널의 최근 메시지를 오래된 순으로 반환합니다.")
    @GetMapping
    public List<ChatMessageResponse> getRecentMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return chatService.getRecentMessages(new RoomId(roomId), limit).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    public record ChatMessageResponse(
            String messageId,
            String roomId,
            String sessionId,
            String senderUserId,
            String payload,
            String sentAt
    ) {
        private static ChatMessageResponse from(ChatMessageResult result) {
            return new ChatMessageResponse(
                    result.messageId().value(),
                    result.roomId().value(),
                    result.sessionId().value(),
                    result.senderUserId().value(),
                    result.payload(),
                    result.sentAt().toString()
            );
        }
    }
}
