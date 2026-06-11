package io.wowtalk.redis.config;

import io.wowtalk.redis.subscriber.RedisRealtimeEventSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@EnableConfigurationProperties(RedisRealtimeProperties.class)
@ConditionalOnProperty(prefix = "wowtalk.realtime", name = "broker", havingValue = "redis")
public class RedisRealtimeConfig {

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisRealtimeEventSubscriber subscriber,
            RedisRealtimeProperties properties
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                subscriber,
                new PatternTopic(properties.resolvedChannelPrefix() + ":*")
        );
        return container;
    }
}
