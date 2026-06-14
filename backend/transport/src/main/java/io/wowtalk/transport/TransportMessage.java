package io.wowtalk.transport;

import java.time.Instant;

/**
 * core에서 생성된 채팅 결과를 transport 구현체가 전송할 수 있는 공통 메시지 형태로 옮긴 값 객체다.
 *
 * <p>requestId는 protocol v1 클라이언트 요청과 서버 이벤트를 연결하기 위한 선택 필드다. legacy
 * WebSocket 응답에서는 비어 있을 수 있다.
 */
public record TransportMessage(
        String messageId,
        RoomId roomId,
        ConnectionId connectionId,
        SessionId sessionId,
        String senderUserId,
        String payload,
        Instant sentAt,
        String requestId
) {

    public TransportMessage(
            String messageId,
            RoomId roomId,
            ConnectionId connectionId,
            SessionId sessionId,
            String senderUserId,
            String payload,
            Instant sentAt
    ) {
        this(messageId, roomId, connectionId, sessionId, senderUserId, payload, sentAt, null);
    }

    public TransportMessage {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("메시지 ID는 필수입니다.");
        }
        if (roomId == null) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다.");
        }
        if (connectionId == null) {
            throw new IllegalArgumentException("연결 ID는 필수입니다.");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("세션 ID는 필수입니다.");
        }
        if (senderUserId == null || senderUserId.isBlank()) {
            throw new IllegalArgumentException("발신자 사용자 ID는 필수입니다.");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }
        if (sentAt == null) {
            sentAt = Instant.now();
        }
    }

    public TransportMessage withRequestId(String requestId) {
        return new TransportMessage(messageId, roomId, connectionId, sessionId, senderUserId, payload, sentAt, requestId);
    }
}
