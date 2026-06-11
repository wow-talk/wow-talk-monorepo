package io.wowtalk.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wowtalk.realtime.redis")
public record RedisRealtimeProperties(
        String channelPrefix
) {

    public String resolvedChannelPrefix() {
        if (channelPrefix == null || channelPrefix.isBlank()) {
            return "wowtalk:room-events";
        }
        return channelPrefix;
    }
}
