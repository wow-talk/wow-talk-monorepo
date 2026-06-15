package io.wowtalk.room.domain;

import io.wowtalk.transport.RoomId;
import java.time.Instant;

/**
 * 채팅 또는 게임이 일어나는 제품 도메인의 방 모델이다.
 *
 * <p>Channel은 이 방의 transport metadata만 담당하고, Room은 방 타입과 상태 같은 제품 규칙을
 * 담당한다.
 */
public record Room(
        RoomId roomId,
        RoomType roomType,
        RoomStatus status,
        int maxMembers,
        Instant createdAt
) {

    public static final int DEFAULT_MAX_MEMBERS = 10;

    public Room {
        if (roomId == null) {
            throw new IllegalArgumentException("방 ID는 필수입니다.");
        }
        if (roomType == null) {
            roomType = RoomType.CHAT;
        }
        if (status == null) {
            status = RoomStatus.WAITING;
        }
        if (maxMembers <= 0) {
            maxMembers = DEFAULT_MAX_MEMBERS;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
