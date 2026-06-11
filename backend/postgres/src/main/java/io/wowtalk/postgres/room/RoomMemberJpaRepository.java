package io.wowtalk.postgres.room;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMemberJpaRepository extends JpaRepository<RoomMemberEntity, Long> {

    Optional<RoomMemberEntity> findByRoomIdAndUserId(String roomId, String userId);
}
