package io.wowtalk.realtime.repository;

import io.wowtalk.realtime.domain.RoomEvent;
import io.wowtalk.transport.RoomId;
import java.util.List;

public interface RoomEventRepository {

    RoomEvent save(RoomEvent roomEvent);

    List<RoomEvent> findRecentByRoomId(RoomId roomId, int limit);
}
