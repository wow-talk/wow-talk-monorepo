package io.wowtalk.realtime.repository;

import io.wowtalk.realtime.domain.RoomEvent;
import io.wowtalk.transport.RoomId;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryRoomEventRepository implements RoomEventRepository {

    private final CopyOnWriteArrayList<RoomEvent> roomEvents = new CopyOnWriteArrayList<>();

    @Override
    public RoomEvent save(RoomEvent roomEvent) {
        roomEvents.add(roomEvent);
        return roomEvent;
    }

    @Override
    public List<RoomEvent> findRecentByRoomId(RoomId roomId, int limit) {
        List<RoomEvent> events = roomEvents.stream()
                .filter(roomEvent -> roomEvent.roomId().equals(roomId))
                .sorted(Comparator.comparing(RoomEvent::occurredAt))
                .toList();

        return events.stream()
                .skip(Math.max(0, events.size() - limit))
                .toList();
    }
}
