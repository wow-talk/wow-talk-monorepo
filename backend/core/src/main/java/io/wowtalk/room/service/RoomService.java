package io.wowtalk.room.service;

import io.wowtalk.room.domain.RoomType;
import io.wowtalk.room.dto.RoomInfo;
import io.wowtalk.transport.RoomId;

public interface RoomService {

    RoomInfo ensureRoom(RoomId roomId);

    RoomInfo create(RoomId roomId, RoomType roomType, int maxMembers);

    RoomInfo get(RoomId roomId);
}
