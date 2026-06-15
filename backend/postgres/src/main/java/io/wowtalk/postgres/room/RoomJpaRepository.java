package io.wowtalk.postgres.room;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomJpaRepository extends JpaRepository<RoomEntity, Long> {

    Optional<RoomEntity> findByRoomId(String roomId);
}
