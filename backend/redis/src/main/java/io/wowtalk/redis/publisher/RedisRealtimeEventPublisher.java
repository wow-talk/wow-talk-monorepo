package io.wowtalk.redis.publisher;

import io.wowtalk.redis.config.RedisRealtimeProperties;
import io.wowtalk.redis.message.RedisRealtimeMessage;
import io.wowtalk.transport.RealtimeEventPublisher;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * 현재 API task에서 발생한 realtime event를 Redis Pub/Sub 채널로 발행한다.
 *
 * <p>Redis는 여기서 영속 저장소가 아니라 여러 API task에 WebSocket broadcast를 전달하는 fan-out
 * 통로로만 사용한다.
 */
@Component
@ConditionalOnProperty(prefix = "wowtalk.realtime", name = "broker", havingValue = "redis")
public class RedisRealtimeEventPublisher implements RealtimeEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisRealtimeProperties properties;

    public RedisRealtimeEventPublisher(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            RedisRealtimeProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void publish(RoomId roomId, TransportMessage message) {
        try {
            redisTemplate.convertAndSend(
                    channel(roomId),
                    objectMapper.writeValueAsString(RedisRealtimeMessage.from(message))
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Redis realtime event 직렬화에 실패했습니다.", exception);
        }
    }

    private String channel(RoomId roomId) {
        return properties.resolvedChannelPrefix() + ":" + roomId.value();
    }
}
