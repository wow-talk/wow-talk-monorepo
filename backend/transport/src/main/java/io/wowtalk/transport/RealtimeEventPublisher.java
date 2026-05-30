package io.wowtalk.transport;

public interface RealtimeEventPublisher {

    void publish(RoomId roomId, TransportMessage message);
}
