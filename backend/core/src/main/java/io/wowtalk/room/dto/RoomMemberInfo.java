package io.wowtalk.room.dto;

import io.wowtalk.room.domain.RoomMemberRole;
import io.wowtalk.room.domain.RoomMemberStatus;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;
import java.time.Instant;

public record RoomMemberInfo(
        RoomId roomId,
        UserId userId,
        RoomMemberRole role,
        RoomMemberStatus status,
        Instant joinedAt
) {
}
