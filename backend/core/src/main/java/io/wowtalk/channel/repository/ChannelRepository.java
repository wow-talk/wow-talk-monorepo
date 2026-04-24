package io.wowtalk.channel.repository;

import io.wowtalk.channel.domain.Channel;
import io.wowtalk.transport.RoomId;
import java.util.Optional;

public interface ChannelRepository {

    Optional<Channel> findByRoomId(RoomId roomId);

    Channel save(Channel channel);
}
