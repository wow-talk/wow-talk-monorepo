package io.wowtalk.chat.gateway;

import io.wowtalk.transport.RealtimeEventPublisher;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.transport.TransportRouter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 단일 API 인스턴스 개발 환경에서 realtime event를 바로 local transport로 전달한다.
 *
 * <p>운영 scale-out 경로는 Redis 같은 broker adapter가 담당하고, local publisher는 로컬 개발과
 * 단일 서버 테스트의 기본값으로만 사용한다.
 */
@Component
@ConditionalOnProperty(prefix = "wowtalk.realtime", name = "broker", havingValue = "local", matchIfMissing = true)
public class LocalRealtimeEventPublisher implements RealtimeEventPublisher {

    private final TransportRouter transportRouter;

    public LocalRealtimeEventPublisher(TransportRouter transportRouter) {
        this.transportRouter = transportRouter;
    }

    @Override
    public void publish(RoomId roomId, TransportMessage message) {
        transportRouter.route(TransportMode.WEBSOCKET).broadcast(roomId, message);
    }
}
