package io.wowtalk.channel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.wowtalk.channel.dto.ChannelTransportInfo;
import io.wowtalk.channel.repository.InMemoryChannelRepository;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMode;
import org.junit.jupiter.api.Test;

class DefaultChannelServiceTest {

    private final DefaultChannelService channelService = new DefaultChannelService(new InMemoryChannelRepository());

    @Test
    void 채널이_없으면_생성한다() {
        RoomId roomId = new RoomId("room-1");

        channelService.ensureChannel(roomId, TransportMode.WEBSOCKET);

        ChannelTransportInfo transportInfo = channelService.getTransportInfo(roomId);
        assertThat(transportInfo.transportMode()).isEqualTo(TransportMode.WEBSOCKET);
    }

    @Test
    void 이미_존재하는_채널과_다른_transportMode를_요청하면_예외가_발생한다() {
        RoomId roomId = new RoomId("room-1");
        channelService.ensureChannel(roomId, TransportMode.WEBSOCKET);

        assertThatThrownBy(() -> channelService.ensureChannel(roomId, TransportMode.RAW_TCP))
                .isInstanceOf(TransportModeMismatchException.class)
                .hasMessage("채널의 전송 방식과 요청한 전송 방식이 다릅니다.");
    }
}
