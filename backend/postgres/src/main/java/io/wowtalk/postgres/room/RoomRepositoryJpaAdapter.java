package io.wowtalk.postgres.room;

import io.wowtalk.room.domain.Room;
import io.wowtalk.room.repository.RoomRepository;
import io.wowtalk.transport.RoomId;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Profile("postgres")
@Primary
@Repository
public class RoomRepositoryJpaAdapter implements RoomRepository {

    private final RoomJpaRepository roomJpaRepository;

    public RoomRepositoryJpaAdapter(RoomJpaRepository roomJpaRepository) {
        this.roomJpaRepository = roomJpaRepository;
    }

    @Override
    public Optional<Room> findByRoomId(RoomId roomId) {
        return roomJpaRepository.findByRoomId(roomId.value())
                .map(this::toDomain);
    }

    @Override
    public Room save(Room room) {
        RoomEntity saved = roomJpaRepository.save(new RoomEntity(
                room.roomId().value(),
                room.roomType(),
                room.status(),
                room.maxMembers(),
                room.createdAt()
        ));
        return toDomain(saved);
    }

    private Room toDomain(RoomEntity entity) {
        return new Room(
                new RoomId(entity.getRoomId()),
                entity.getRoomType(),
                entity.getStatus(),
                entity.getMaxMembers(),
                entity.getCreatedAt()
        );
    }
}
