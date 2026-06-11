package io.wowtalk.postgres.channel;

import io.wowtalk.channel.domain.Channel;
import io.wowtalk.channel.repository.ChannelRepository;
import io.wowtalk.transport.RoomId;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Profile("postgres")
@Primary
@Repository
public class ChannelRepositoryJpaAdapter implements ChannelRepository {

    private final ChannelJpaRepository channelJpaRepository;

    public ChannelRepositoryJpaAdapter(ChannelJpaRepository channelJpaRepository) {
        this.channelJpaRepository = channelJpaRepository;
    }

    @Override
    public Optional<Channel> findByRoomId(RoomId roomId) {
        return channelJpaRepository.findByRoomId(roomId.value())
                .map(entity -> new Channel(new RoomId(entity.getRoomId()), entity.getTransportMode()));
    }

    @Override
    public Channel save(Channel channel) {
        ChannelEntity saved = channelJpaRepository.save(new ChannelEntity(channel.roomId().value(), channel.transportMode()));
        return new Channel(new RoomId(saved.getRoomId()), saved.getTransportMode());
    }
}
