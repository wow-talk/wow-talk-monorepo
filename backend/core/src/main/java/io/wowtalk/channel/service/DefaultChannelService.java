package io.wowtalk.channel.service;

import io.wowtalk.channel.domain.Channel;
import io.wowtalk.channel.dto.ChannelTransportInfo;
import io.wowtalk.channel.repository.ChannelRepository;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class DefaultChannelService implements ChannelService {

    private final ChannelRepository channelRepository;

    public DefaultChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public void ensureChannel(RoomId roomId, TransportMode transportMode) {
        channelRepository.findByRoomId(roomId)
                .ifPresentOrElse(channel -> validateTransportMode(channel, transportMode),
                        () -> createChannel(roomId, transportMode));
    }

    @Override
    public ChannelTransportInfo getTransportInfo(RoomId roomId) {
        Channel channel = channelRepository.findByRoomId(roomId)
                .orElseThrow(ChannelNotFoundException::new);

        return new ChannelTransportInfo(channel.roomId(), channel.transportMode());
    }

    private void validateTransportMode(Channel channel, TransportMode transportMode) {
        if (channel.transportMode() != transportMode) {
            throw new TransportModeMismatchException();
        }
    }

    private void createChannel(RoomId roomId, TransportMode transportMode) {
        try {
            channelRepository.save(new Channel(roomId, transportMode));
        } catch (DataIntegrityViolationException e) {
            Channel existing = channelRepository.findByRoomId(roomId)
                    .orElseThrow(() -> e);
            validateTransportMode(existing, transportMode);
        }
    }
}
