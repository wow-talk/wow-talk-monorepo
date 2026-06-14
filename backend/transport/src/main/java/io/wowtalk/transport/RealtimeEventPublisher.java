package io.wowtalk.transport;

/**
 * 한 API task에서 만들어진 realtime event를 필요한 transport fan-out 경로로 전달하는 포트다.
 *
 * <p>local broker는 같은 JVM의 WebSocket registry로 바로 broadcast하고, Redis broker는 다른 API
 * task까지 이벤트를 전달한다. core service는 이 fan-out 방식을 알지 않는다.
 */
public interface RealtimeEventPublisher {

    void publish(RoomId roomId, TransportMessage message);
}
