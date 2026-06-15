package io.wowtalk.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wowtalk.room.domain.RoomType;
import io.wowtalk.room.dto.RoomInfo;
import io.wowtalk.room.service.RoomService;
import io.wowtalk.transport.RoomId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Room", description = "방 관리 API")
@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "방 생성 또는 조회", description = "방이 없으면 생성하고, 있으면 기존 방 정보를 반환합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody CreateRoomRequest request) {
        return RoomResponse.from(roomService.create(
                new RoomId(request.roomId()),
                request.roomType(),
                request.maxMembers() == null ? 0 : request.maxMembers()
        ));
    }

    @Operation(summary = "방 조회", description = "방의 제품 메타데이터를 조회합니다.")
    @GetMapping("/{roomId}")
    public RoomResponse get(@PathVariable String roomId) {
        return RoomResponse.from(roomService.get(new RoomId(roomId)));
    }

    public record CreateRoomRequest(
            @NotBlank(message = "방 ID는 필수입니다.")
            String roomId,
            RoomType roomType,
            @Positive(message = "최대 참여자 수는 1 이상이어야 합니다.")
            Integer maxMembers
    ) {
    }

    public record RoomResponse(
            String roomId,
            String roomType,
            String status,
            int maxMembers,
            String createdAt
    ) {
        private static RoomResponse from(RoomInfo info) {
            return new RoomResponse(
                    info.roomId().value(),
                    info.roomType().name(),
                    info.status().name(),
                    info.maxMembers(),
                    info.createdAt().toString()
            );
        }
    }
}
