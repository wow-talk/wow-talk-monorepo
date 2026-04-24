package io.wowtalk.message.repository;

import io.wowtalk.message.domain.ChatMessageEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findByRoomIdOrderBySentAtDesc(String roomId, Pageable pageable);
}
