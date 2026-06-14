package io.wowtalk.channel.domain;

import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMode;

/**
 * 방이 사용할 realtime transport 방식을 나타내는 채널 메타데이터다.
 *
 * <p>현재는 WebSocket 중심이지만, Raw TCP 같은 다른 transport를 비교하거나 확장할 수 있도록
 * roomId와 transportMode를 core 도메인에 명시한다.
 */
public record Channel(
        RoomId roomId,
        TransportMode transportMode
) {
}
