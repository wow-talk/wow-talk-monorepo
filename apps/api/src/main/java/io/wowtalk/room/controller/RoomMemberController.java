package io.wowtalk.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wowtalk.room.dto.RoomMemberInfo;
import io.wowtalk.room.service.RoomMemberService;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Room Member", description = "방 참여 API")
@RestController
@RequestMapping("/api/v1/rooms/{roomId}/members")
public class RoomMemberController {

    private final RoomMemberService roomMemberService;

    public RoomMemberController(RoomMemberService roomMemberService) {
        this.roomMemberService = roomMemberService;
    }

    @Operation(summary = "방 참여", description = "사용자를 방 참여자로 등록합니다. 방이 없으면 WebSocket 방으로 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomMemberResponse join(
            @PathVariable String roomId,
            @Valid @RequestBody JoinRoomRequest request
    ) {
        return RoomMemberResponse.from(roomMemberService.join(new RoomId(roomId), new UserId(request.userId())));
    }

    public record JoinRoomRequest(
            @NotBlank(message = "사용자 ID는 필수입니다.")
            String userId
    ) {
    }

    public record RoomMemberResponse(
            String roomId,
            String userId,
            String role,
            String status,
            String joinedAt
    ) {
        private static RoomMemberResponse from(RoomMemberInfo info) {
            return new RoomMemberResponse(
                    info.roomId().value(),
                    info.userId().value(),
                    info.role().name(),
                    info.status().name(),
                    info.joinedAt().toString()
            );
        }
    }
}
