package io.wowtalk.room.service;

import io.wowtalk.room.domain.Room;
import io.wowtalk.room.domain.RoomType;
import io.wowtalk.room.dto.RoomInfo;
import io.wowtalk.room.repository.RoomRepository;
import io.wowtalk.transport.RoomId;
import org.springframework.stereotype.Service;

@Service
public class DefaultRoomService implements RoomService {

    private final RoomRepository roomRepository;

    public DefaultRoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomInfo ensureRoom(RoomId roomId) {
        return roomRepository.findByRoomId(roomId)
                .map(this::toInfo)
                .orElseGet(() -> toInfo(roomRepository.save(new Room(roomId, null, null, 0, null))));
    }

    @Override
    public RoomInfo create(RoomId roomId, RoomType roomType, int maxMembers) {
        return roomRepository.findByRoomId(roomId)
                .map(this::toInfo)
                .orElseGet(() -> toInfo(roomRepository.save(new Room(roomId, roomType, null, maxMembers, null))));
    }

    @Override
    public RoomInfo get(RoomId roomId) {
        return roomRepository.findByRoomId(roomId)
                .map(this::toInfo)
                .orElseThrow(RoomNotFoundException::new);
    }

    private RoomInfo toInfo(Room room) {
        return new RoomInfo(
                room.roomId(),
                room.roomType(),
                room.status(),
                room.maxMembers(),
                room.createdAt()
        );
    }
}
