package io.wowtalk.realtime.repository;

import io.wowtalk.realtime.domain.RoomEvent;
import io.wowtalk.transport.RoomId;
import java.util.List;

/**
 * 방 단위 realtime event stream을 저장하고 조회하는 포트다.
 *
 * <p>채팅 메시지 저장소와 분리해 두면 이후 게임 이벤트, 시스템 이벤트, 상태 패치가 늘어나도
 * room event stream access pattern을 독립적으로 발전시킬 수 있다.
 */
public interface RoomEventRepository {

    RoomEvent save(RoomEvent roomEvent);

    List<RoomEvent> findRecentByRoomId(RoomId roomId, int limit);
}
