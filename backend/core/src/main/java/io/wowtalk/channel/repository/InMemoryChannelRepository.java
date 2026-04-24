package io.wowtalk.channel.repository;

import io.wowtalk.channel.domain.Channel;
import io.wowtalk.transport.RoomId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryChannelRepository implements ChannelRepository {

    private final Map<RoomId, Channel> channels = new ConcurrentHashMap<>();

    @Override
    public Optional<Channel> findByRoomId(RoomId roomId) {
        return Optional.ofNullable(channels.get(roomId));
    }

    @Override
    public Channel save(Channel channel) {
        channels.put(channel.roomId(), channel);
        return channel;
    }
}
