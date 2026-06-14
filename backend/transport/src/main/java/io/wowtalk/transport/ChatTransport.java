package io.wowtalk.transport;

/**
 * 채팅 이벤트를 실제 클라이언트 연결로 내보내는 transport adapter 계약이다.
 *
 * <p>WebSocket, Raw TCP 같은 구현체는 이 인터페이스를 구현하고, core는 구체 연결 기술을 알지 않는다.
 */
public interface ChatTransport {

    TransportMode mode();

    void sendToSession(TransportMessage message);

    void broadcast(RoomId roomId, TransportMessage message);
}
