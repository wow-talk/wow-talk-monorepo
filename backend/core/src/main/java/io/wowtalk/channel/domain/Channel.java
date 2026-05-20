package io.wowtalk.channel.domain;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMode;

public record Channel(
        RoomId roomId,
        TransportMode transportMode
) {
}
