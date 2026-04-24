package io.wowtalk.channel.dto;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMode;

public record ChannelTransportInfo(
        RoomId roomId,
        TransportMode transportMode
) {
}
