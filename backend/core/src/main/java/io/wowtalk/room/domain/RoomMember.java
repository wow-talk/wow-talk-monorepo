package io.wowtalk.room.domain;

import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;
import java.time.Instant;

/**
 * 사용자가 특정 방에 어떤 권한과 상태로 참여했는지 나타내는 도메인 모델이다.
 *
 * <p>게임 참가, 방 권한, 추후 강퇴/퇴장 상태를 같은 흐름에서 다루기 위한 기반이다.
 */
public record RoomMember(
        RoomId roomId,
        UserId userId,
        RoomMemberRole role,
        RoomMemberStatus status,
        Instant joinedAt
) {

    public RoomMember {
        if (roomId == null) {
            throw new IllegalArgumentException("방 ID는 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (role == null) {
            role = RoomMemberRole.MEMBER;
        }
        if (status == null) {
            status = RoomMemberStatus.ACTIVE;
        }
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}
