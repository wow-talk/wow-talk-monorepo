package io.wowtalk.room.repository;

import io.wowtalk.room.domain.Room;
import io.wowtalk.transport.RoomId;
import java.util.Optional;

/**
 * Room 제품 도메인의 영속성 포트다.
 *
 * <p>Room은 방 상태와 타입을 담당하고, Channel은 같은 roomId의 transport metadata를 담당한다.
 */
public interface RoomRepository {

    Optional<Room> findByRoomId(RoomId roomId);

    Room save(Room room);
}
