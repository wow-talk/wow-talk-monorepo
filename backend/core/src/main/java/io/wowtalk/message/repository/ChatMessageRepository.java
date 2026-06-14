package io.wowtalk.message.repository;

import io.wowtalk.message.domain.ChatMessage;
import io.wowtalk.transport.RoomId;
import java.util.List;

/**
 * 채팅 메시지를 저장하고 조회하는 core 영속성 포트다.
 *
 * <p>core는 이 인터페이스까지만 알고, DynamoDB/Postgres/in-memory 같은 실제 저장 방식은
 * adapter 모듈이 구현한다.
 */
public interface ChatMessageRepository {

    ChatMessage save(ChatMessage chatMessage);

    List<ChatMessage> findAll();

    List<ChatMessage> findRecentByRoomId(RoomId roomId, int limit);
}
