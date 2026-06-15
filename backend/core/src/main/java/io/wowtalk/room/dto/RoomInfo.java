package io.wowtalk.room.dto;

import io.wowtalk.room.domain.RoomStatus;
import io.wowtalk.room.domain.RoomType;
import io.wowtalk.transport.RoomId;
import java.time.Instant;

public record RoomInfo(
        RoomId roomId,
        RoomType roomType,
        RoomStatus status,
        int maxMembers,
        Instant createdAt
) {
}
