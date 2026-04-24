package io.wowtalk.channel.repository;

import io.wowtalk.channel.domain.ChannelEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelJpaRepository extends JpaRepository<ChannelEntity, Long> {

    Optional<ChannelEntity> findByRoomId(String roomId);
}
