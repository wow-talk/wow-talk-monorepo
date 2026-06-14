package io.wowtalk.channel.repository;

import io.wowtalk.channel.domain.Channel;
import io.wowtalk.transport.RoomId;
import java.util.Optional;

/**
 * 방의 채팅 채널 메타데이터를 다루는 영속성 포트다.
 *
 * <p>현재 Channel은 roomId와 transportMode를 묶어, 방마다 어떤 realtime transport를 사용할지
 * core service가 구현체를 모르고 판단하게 해준다.
 */
public interface ChannelRepository {

    Optional<Channel> findByRoomId(RoomId roomId);

    Channel save(Channel channel);
}
