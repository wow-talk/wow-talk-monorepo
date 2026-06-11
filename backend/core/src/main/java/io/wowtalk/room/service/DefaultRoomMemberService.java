package io.wowtalk.room.service;

import io.wowtalk.channel.service.ChannelService;
import io.wowtalk.room.domain.RoomMember;
import io.wowtalk.room.dto.RoomMemberInfo;
import io.wowtalk.room.repository.RoomMemberRepository;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.user.domain.UserId;
import io.wowtalk.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class DefaultRoomMemberService implements RoomMemberService {

    private final ChannelService channelService;
    private final UserService userService;
    private final RoomMemberRepository roomMemberRepository;

    public DefaultRoomMemberService(
            ChannelService channelService,
            UserService userService,
            RoomMemberRepository roomMemberRepository
    ) {
        this.channelService = channelService;
        this.userService = userService;
        this.roomMemberRepository = roomMemberRepository;
    }

    @Override
    public RoomMemberInfo join(RoomId roomId, UserId userId) {
        channelService.ensureChannel(roomId, TransportMode.WEBSOCKET);
        userService.get(userId);

        return roomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .map(this::toInfo)
                .orElseGet(() -> create(roomId, userId));
    }

    private RoomMemberInfo create(RoomId roomId, UserId userId) {
        return toInfo(roomMemberRepository.save(new RoomMember(roomId, userId, null, null, null)));
    }

    private RoomMemberInfo toInfo(RoomMember roomMember) {
        return new RoomMemberInfo(
                roomMember.roomId(),
                roomMember.userId(),
                roomMember.role(),
                roomMember.status(),
                roomMember.joinedAt()
        );
    }
}
