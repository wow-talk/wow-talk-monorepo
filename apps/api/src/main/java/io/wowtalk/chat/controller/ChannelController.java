package io.wowtalk.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wowtalk.channel.dto.ChannelTransportInfo;
import io.wowtalk.channel.service.ChannelService;
import io.wowtalk.transport.RoomId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Channel", description = "채널 관리 API")
@RestController
@RequestMapping("/api/v1/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Operation(summary = "채널 생성 또는 보장", description = "채널이 없으면 생성하고, 있으면 전송 방식 일치 여부를 확인합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChannelResponse create(@Valid @RequestBody CreateChannelRequest request) {
        RoomId roomId = new RoomId(request.roomId());
        channelService.ensureChannel(roomId, request.transportMode());
        return ChannelResponse.from(channelService.getTransportInfo(roomId));
    }

    @Operation(summary = "채널 조회", description = "채널의 transport 모드를 조회합니다.")
    @GetMapping("/{roomId}")
    public ChannelResponse get(@PathVariable String roomId) {
        return ChannelResponse.from(channelService.getTransportInfo(new RoomId(roomId)));
    }

    public record CreateChannelRequest(
            @NotBlank(message = "채널 ID는 필수입니다.")
            String roomId,
            io.wowtalk.transport.TransportMode transportMode
    ) {
    }

    public record ChannelResponse(
            String roomId,
            String transportMode
    ) {
        private static ChannelResponse from(ChannelTransportInfo info) {
            return new ChannelResponse(info.roomId().value(), info.transportMode().name());
        }
    }
}
