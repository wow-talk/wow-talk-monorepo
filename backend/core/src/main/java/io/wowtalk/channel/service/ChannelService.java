package io.wowtalk.channel.service;

import io.wowtalk.channel.dto.ChannelTransportInfo;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMode;

public interface ChannelService {

    void ensureChannel(RoomId roomId, TransportMode transportMode);

    ChannelTransportInfo getTransportInfo(RoomId roomId);
}
