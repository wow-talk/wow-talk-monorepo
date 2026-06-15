package io.wowtalk.room.repository;

import io.wowtalk.room.domain.Room;
import io.wowtalk.transport.RoomId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRoomRepository implements RoomRepository {

    private final Map<RoomId, Room> rooms = new ConcurrentHashMap<>();

    @Override
    public Optional<Room> findByRoomId(RoomId roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    @Override
    public Room save(Room room) {
        rooms.put(room.roomId(), room);
        return room;
    }
}
