package io.wowtalk.transport;

public interface ChatTransport {

    TransportMode mode();

    void sendToSession(TransportMessage message);

    void broadcast(RoomId roomId, TransportMessage message);
}
