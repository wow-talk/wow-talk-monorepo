package io.wowtalk.room.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.wowtalk.room.domain.Room;
import io.wowtalk.room.domain.RoomStatus;
import io.wowtalk.room.domain.RoomType;
import io.wowtalk.room.dto.RoomInfo;
import io.wowtalk.room.repository.InMemoryRoomRepository;
import io.wowtalk.transport.RoomId;
import org.junit.jupiter.api.Test;

class DefaultRoomServiceTest {

    private final DefaultRoomService roomService = new DefaultRoomService(new InMemoryRoomRepository());

    @Test
    void 방을_생성한다() {
        RoomInfo room = roomService.create(new RoomId("room-1"), RoomType.GAME, 8);

        assertThat(room.roomId()).isEqualTo(new RoomId("room-1"));
        assertThat(room.roomType()).isEqualTo(RoomType.GAME);
        assertThat(room.status()).isEqualTo(RoomStatus.WAITING);
        assertThat(room.maxMembers()).isEqualTo(8);
        assertThat(room.createdAt()).isNotNull();
    }

    @Test
    void 방이_없으면_기본_채팅방으로_보장한다() {
        RoomInfo room = roomService.ensureRoom(new RoomId("room-1"));

        assertThat(room.roomType()).isEqualTo(RoomType.CHAT);
        assertThat(room.maxMembers()).isEqualTo(Room.DEFAULT_MAX_MEMBERS);
    }

    @Test
    void 없는_방을_조회하면_예외가_발생한다() {
        assertThatThrownBy(() -> roomService.get(new RoomId("missing-room")))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessage("방을 찾을 수 없습니다.");
    }
}
