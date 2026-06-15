package io.wowtalk.room.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.room.domain.RoomType;
import io.wowtalk.room.repository.InMemoryRoomRepository;
import io.wowtalk.room.service.DefaultRoomService;
import org.junit.jupiter.api.Test;

class RoomControllerTest {

    private final RoomController controller = new RoomController(new DefaultRoomService(new InMemoryRoomRepository()));

    @Test
    void 방을_생성하면_방_계약_필드를_반환한다() {
        RoomController.RoomResponse response = controller.create(new RoomController.CreateRoomRequest(
                "frontend-contract-room",
                RoomType.GAME,
                8
        ));

        assertThat(response.roomId()).isEqualTo("frontend-contract-room");
        assertThat(response.roomType()).isEqualTo("GAME");
        assertThat(response.status()).isEqualTo("WAITING");
        assertThat(response.maxMembers()).isEqualTo(8);
        assertThat(response.createdAt()).isNotBlank();
    }
}
