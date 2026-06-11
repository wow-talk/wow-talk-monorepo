package io.wowtalk.dynamodb.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.wowtalk.dynamodb.realtime.RoomEventDynamoDbRepository;
import io.wowtalk.dynamodb.realtime.RoomEventDynamoDbTableInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

class DynamoDbConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    DynamoDbConfig.class,
                    RoomEventDynamoDbRepository.class,
                    RoomEventDynamoDbTableInitializer.class
            );

    @Test
    void createsDynamoDbBeansWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "wowtalk.dynamodb.enabled=true",
                        "wowtalk.dynamodb.initialize-schema=true",
                        "wowtalk.dynamodb.endpoint=http://localhost:8000",
                        "wowtalk.dynamodb.region=ap-northeast-2",
                        "wowtalk.dynamodb.room-events-table=wowtalk-room-events-local"
                )
                .run(context -> assertThat(context)
                        .hasSingleBean(DynamoDbClient.class)
                        .hasSingleBean(RoomEventDynamoDbRepository.class)
                        .hasSingleBean(RoomEventDynamoDbTableInitializer.class));
    }

    @Test
    void doesNotCreateDynamoDbBeansWhenDisabled() {
        contextRunner
                .withPropertyValues(
                        "wowtalk.dynamodb.enabled=false",
                        "wowtalk.dynamodb.initialize-schema=false"
                )
                .run(context -> assertThat(context)
                        .doesNotHaveBean(DynamoDbClient.class)
                        .doesNotHaveBean(RoomEventDynamoDbRepository.class)
                        .doesNotHaveBean(RoomEventDynamoDbTableInitializer.class));
    }
}
