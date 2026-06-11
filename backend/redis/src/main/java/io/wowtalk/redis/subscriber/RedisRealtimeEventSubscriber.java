package io.wowtalk.redis.subscriber;

import io.wowtalk.redis.message.RedisRealtimeMessage;
import io.wowtalk.transport.TransportMessage;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.transport.TransportRouter;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "wowtalk.realtime", name = "broker", havingValue = "redis")
public class RedisRealtimeEventSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final TransportRouter transportRouter;

    public RedisRealtimeEventSubscriber(ObjectMapper objectMapper, TransportRouter transportRouter) {
        this.objectMapper = objectMapper;
        this.transportRouter = transportRouter;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            TransportMessage transportMessage = objectMapper
                    .readValue(new String(message.getBody(), StandardCharsets.UTF_8), RedisRealtimeMessage.class)
                    .toTransportMessage();

            transportRouter
                    .route(TransportMode.WEBSOCKET)
                    .broadcast(transportMessage.roomId(), transportMessage);
        } catch (Exception exception) {
            throw new IllegalStateException("Redis realtime event 역직렬화에 실패했습니다.", exception);
        }
    }
}
