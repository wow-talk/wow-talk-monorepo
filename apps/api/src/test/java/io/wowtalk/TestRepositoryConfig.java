package io.wowtalk;

import io.wowtalk.channel.repository.ChannelRepository;
import io.wowtalk.channel.repository.InMemoryChannelRepository;
import io.wowtalk.message.repository.ChatMessageRepository;
import io.wowtalk.message.repository.InMemoryChatMessageRepository;
import io.wowtalk.room.repository.InMemoryRoomMemberRepository;
import io.wowtalk.room.repository.InMemoryRoomRepository;
import io.wowtalk.room.repository.RoomMemberRepository;
import io.wowtalk.room.repository.RoomRepository;
import io.wowtalk.realtime.repository.InMemoryRoomEventRepository;
import io.wowtalk.realtime.repository.RoomEventRepository;
import io.wowtalk.user.repository.AuthIdentityRepository;
import io.wowtalk.user.repository.InMemoryAuthIdentityRepository;
import io.wowtalk.user.repository.InMemoryUserRepository;
import io.wowtalk.user.repository.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRepositoryConfig {

    @Bean
    ChannelRepository channelRepository() {
        return new InMemoryChannelRepository();
    }

    @Bean
    ChatMessageRepository chatMessageRepository() {
        return new InMemoryChatMessageRepository();
    }

    @Bean
    UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    AuthIdentityRepository authIdentityRepository() {
        return new InMemoryAuthIdentityRepository();
    }

    @Bean
    RoomMemberRepository roomMemberRepository() {
        return new InMemoryRoomMemberRepository();
    }

    @Bean
    RoomRepository roomRepository() {
        return new InMemoryRoomRepository();
    }

    @Bean
    RoomEventRepository roomEventRepository() {
        return new InMemoryRoomEventRepository();
    }
}
