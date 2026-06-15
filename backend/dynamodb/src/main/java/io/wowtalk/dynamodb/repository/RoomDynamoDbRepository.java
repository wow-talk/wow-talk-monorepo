package io.wowtalk.dynamodb.repository;

import io.wowtalk.dynamodb.config.DynamoDbProperties;
import io.wowtalk.dynamodb.support.DynamoDbTableNames;
import io.wowtalk.room.domain.Room;
import io.wowtalk.room.domain.RoomStatus;
import io.wowtalk.room.domain.RoomType;
import io.wowtalk.room.repository.RoomRepository;
import io.wowtalk.transport.RoomId;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * Room 제품 메타데이터를 DynamoDB main table에 저장하는 adapter 구현체다.
 */
@Repository
@ConditionalOnProperty(prefix = "wowtalk.dynamodb", name = "enabled", havingValue = "true")
public class RoomDynamoDbRepository implements RoomRepository {

    private static final String ROOM_ID = "roomId";
    private static final String ROOM_TYPE = "roomType";
    private static final String STATUS = "status";
    private static final String MAX_MEMBERS = "maxMembers";
    private static final String CREATED_AT = "createdAt";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;

    public RoomDynamoDbRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public Optional<Room> findByRoomId(RoomId roomId) {
        Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
                        .tableName(DynamoDbTableNames.mainTable(properties))
                        .key(Map.of(
                                DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.roomPk(roomId)),
                                DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.roomSk())
                        ))
                        .build())
                .item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toDomain(item));
    }

    @Override
    public Room save(Room room) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(DynamoDbTableNames.mainTable(properties))
                .item(Map.of(
                        DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.roomPk(room.roomId())),
                        DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.roomSk()),
                        ROOM_ID, AttributeValue.fromS(room.roomId().value()),
                        ROOM_TYPE, AttributeValue.fromS(room.roomType().name()),
                        STATUS, AttributeValue.fromS(room.status().name()),
                        MAX_MEMBERS, AttributeValue.fromN(String.valueOf(room.maxMembers())),
                        CREATED_AT, AttributeValue.fromS(room.createdAt().toString())
                ))
                .build());
        return room;
    }

    private Room toDomain(Map<String, AttributeValue> item) {
        return new Room(
                new RoomId(item.get(ROOM_ID).s()),
                RoomType.valueOf(item.get(ROOM_TYPE).s()),
                RoomStatus.valueOf(item.get(STATUS).s()),
                Integer.parseInt(item.get(MAX_MEMBERS).n()),
                Instant.parse(item.get(CREATED_AT).s())
        );
    }
}
