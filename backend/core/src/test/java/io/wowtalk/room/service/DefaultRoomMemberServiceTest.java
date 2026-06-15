package io.wowtalk.room.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.channel.repository.InMemoryChannelRepository;
import io.wowtalk.channel.service.DefaultChannelService;
import io.wowtalk.room.domain.RoomMemberRole;
import io.wowtalk.room.domain.RoomMemberStatus;
import io.wowtalk.room.dto.RoomMemberInfo;
import io.wowtalk.room.repository.InMemoryRoomMemberRepository;
import io.wowtalk.room.repository.InMemoryRoomRepository;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.User;
import io.wowtalk.user.repository.InMemoryAuthIdentityRepository;
import io.wowtalk.user.repository.InMemoryUserRepository;
import io.wowtalk.user.service.DefaultUserService;
import org.junit.jupiter.api.Test;

class DefaultRoomMemberServiceTest {

    private final DefaultChannelService channelService = new DefaultChannelService(new InMemoryChannelRepository());
    private final DefaultRoomService roomService = new DefaultRoomService(new InMemoryRoomRepository());
    private final DefaultUserService userService =
            new DefaultUserService(new InMemoryUserRepository(), new InMemoryAuthIdentityRepository());
    private final DefaultRoomMemberService roomMemberService =
            new DefaultRoomMemberService(channelService, roomService, userService, new InMemoryRoomMemberRepository());

    @Test
    void 방_참여자를_등록한다() {
        User user = userService.createGuest("player");

        RoomMemberInfo member = roomMemberService.join(new RoomId("room-1"), user.userId());

        assertThat(member.roomId()).isEqualTo(new RoomId("room-1"));
        assertThat(member.userId()).isEqualTo(user.userId());
        assertThat(member.role()).isEqualTo(RoomMemberRole.MEMBER);
        assertThat(member.status()).isEqualTo(RoomMemberStatus.ACTIVE);
        assertThat(member.joinedAt()).isNotNull();
    }

    @Test
    void 이미_참여한_사용자는_기존_참여_정보를_반환한다() {
        User user = userService.createGuest("player");
        RoomMemberInfo first = roomMemberService.join(new RoomId("room-1"), user.userId());

        RoomMemberInfo second = roomMemberService.join(new RoomId("room-1"), user.userId());

        assertThat(second).isEqualTo(first);
    }
}
