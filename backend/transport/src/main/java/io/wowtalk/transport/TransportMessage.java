package io.wowtalk.transport;

import java.time.Instant;

public record TransportMessage(
        String messageId,
        RoomId roomId,
        SessionId sessionId,
        String payload,
        Instant sentAt
) {

    public TransportMessage {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("메시지 ID는 필수입니다.");
        }
        if (roomId == null) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다.");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("세션 ID는 필수입니다.");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }
        if (sentAt == null) {
            sentAt = Instant.now();
        }
    }
}
