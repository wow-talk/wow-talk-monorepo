package io.wowtalk.chat.gateway;

import io.wowtalk.transport.RealtimeEventPublisher;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.transport.TransportRouter;
import org.springframework.stereotype.Component;

@Component
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
