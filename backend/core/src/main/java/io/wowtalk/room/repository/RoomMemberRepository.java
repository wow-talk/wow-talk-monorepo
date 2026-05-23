package io.wowtalk.room.repository;

import io.wowtalk.room.domain.RoomMember;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;
import java.util.Optional;

public interface RoomMemberRepository {

    Optional<RoomMember> findByRoomIdAndUserId(RoomId roomId, UserId userId);

    RoomMember save(RoomMember roomMember);
}
