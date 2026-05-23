package io.wowtalk.room.service;

import io.wowtalk.room.dto.RoomMemberInfo;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;

public interface RoomMemberService {

    RoomMemberInfo join(RoomId roomId, UserId userId);
}
