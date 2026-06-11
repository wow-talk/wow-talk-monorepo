package io.wowtalk.room.repository;

import io.wowtalk.room.domain.RoomMember;
import io.wowtalk.room.domain.RoomMemberEntity;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Profile("postgres")
@Primary
@Repository
public class RoomMemberRepositoryJpaAdapter implements RoomMemberRepository {

    private final RoomMemberJpaRepository roomMemberJpaRepository;

    public RoomMemberRepositoryJpaAdapter(RoomMemberJpaRepository roomMemberJpaRepository) {
        this.roomMemberJpaRepository = roomMemberJpaRepository;
    }

    @Override
    public Optional<RoomMember> findByRoomIdAndUserId(RoomId roomId, UserId userId) {
        return roomMemberJpaRepository.findByRoomIdAndUserId(roomId.value(), userId.value())
                .map(this::toDomain);
    }

    @Override
    public RoomMember save(RoomMember roomMember) {
        RoomMemberEntity saved = roomMemberJpaRepository.save(new RoomMemberEntity(
                roomMember.roomId().value(),
                roomMember.userId().value(),
                roomMember.role(),
                roomMember.status(),
                roomMember.joinedAt()
        ));
        return toDomain(saved);
    }

    private RoomMember toDomain(RoomMemberEntity entity) {
        return new RoomMember(
                new RoomId(entity.getRoomId()),
                new UserId(entity.getUserId()),
                entity.getRole(),
                entity.getStatus(),
                entity.getJoinedAt()
        );
    }
}
