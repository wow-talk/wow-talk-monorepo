package io.wowtalk.message.domain;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.user.domain.UserId;
import java.time.Instant;

/**
 * 방 안에서 생성된 채팅 메시지 도메인 모델이다.
 *
 * <p>sessionId는 연결 호환성을 위해 남아 있고, 실제 발신자 식별은 senderUserId를 기준으로 한다.
 * 신규 기능은 senderUserId를 우선 사용해야 한다.
 */
public record ChatMessage(
        MessageId messageId,
        RoomId roomId,
        SessionId sessionId,
        UserId senderUserId,
        String payload,
        Instant sentAt
) {

    public ChatMessage {
        if (messageId == null) {
            messageId = MessageId.newId();
        }
        if (senderUserId == null) {
            /*
             * legacy 클라이언트는 sessionId를 발신자처럼 사용했다.
             * 프론트가 userId로 완전히 전환될 때까지만 허용하는 호환 경로다.
             */
            senderUserId = new UserId(sessionId.value());
        }
    }
}
