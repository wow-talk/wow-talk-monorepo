package io.wowtalk.dynamodb.repository;

import io.wowtalk.channel.domain.Channel;
import io.wowtalk.channel.repository.ChannelRepository;
import io.wowtalk.dynamodb.config.DynamoDbProperties;
import io.wowtalk.dynamodb.support.DynamoDbTableNames;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.TransportMode;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Repository
@ConditionalOnProperty(prefix = "wowtalk.dynamodb", name = "enabled", havingValue = "true")
public class ChannelDynamoDbRepository implements ChannelRepository {

    private static final String ROOM_ID = "roomId";
    private static final String TRANSPORT_MODE = "transportMode";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;

    public ChannelDynamoDbRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public Optional<Channel> findByRoomId(RoomId roomId) {
        Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
                        .tableName(DynamoDbTableNames.mainTable(properties))
                        .key(Map.of(
                                DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.roomPk(roomId)),
                                DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.channelSk())
                        ))
                        .build())
                .item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toDomain(item));
    }

    @Override
    public Channel save(Channel channel) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(DynamoDbTableNames.mainTable(properties))
                .item(Map.of(
                        DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.roomPk(channel.roomId())),
                        DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.channelSk()),
                        ROOM_ID, AttributeValue.fromS(channel.roomId().value()),
                        TRANSPORT_MODE, AttributeValue.fromS(channel.transportMode().name())
                ))
                .build());
        return channel;
    }

    private Channel toDomain(Map<String, AttributeValue> item) {
        return new Channel(
                new RoomId(item.get(ROOM_ID).s()),
                TransportMode.valueOf(item.get(TRANSPORT_MODE).s())
        );
    }
}
