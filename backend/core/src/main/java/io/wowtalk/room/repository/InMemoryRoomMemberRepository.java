package io.wowtalk.room.repository;

import io.wowtalk.room.domain.RoomMember;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRoomMemberRepository implements RoomMemberRepository {

    private final Map<Key, RoomMember> roomMembers = new ConcurrentHashMap<>();

    @Override
    public Optional<RoomMember> findByRoomIdAndUserId(RoomId roomId, UserId userId) {
        return Optional.ofNullable(roomMembers.get(new Key(roomId, userId)));
    }

    @Override
    public RoomMember save(RoomMember roomMember) {
        roomMembers.put(new Key(roomMember.roomId(), roomMember.userId()), roomMember);
        return roomMember;
    }

    private record Key(RoomId roomId, UserId userId) {
    }
}
