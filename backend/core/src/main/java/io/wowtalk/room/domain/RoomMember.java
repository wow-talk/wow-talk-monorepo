package io.wowtalk.room.domain;

import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;
import java.time.Instant;

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
