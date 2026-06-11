package io.wowtalk.dynamodb.repository;

import io.wowtalk.dynamodb.config.DynamoDbProperties;
import io.wowtalk.dynamodb.support.DynamoDbTableNames;
import io.wowtalk.room.domain.RoomMember;
import io.wowtalk.room.domain.RoomMemberRole;
import io.wowtalk.room.domain.RoomMemberStatus;
import io.wowtalk.room.repository.RoomMemberRepository;
import io.wowtalk.transport.RoomId;
import io.wowtalk.user.domain.UserId;
import java.time.Instant;
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
public class RoomMemberDynamoDbRepository implements RoomMemberRepository {

    private static final String ROOM_ID = "roomId";
    private static final String USER_ID = "userId";
    private static final String ROLE = "role";
    private static final String STATUS = "status";
    private static final String JOINED_AT = "joinedAt";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;

    public RoomMemberDynamoDbRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public Optional<RoomMember> findByRoomIdAndUserId(RoomId roomId, UserId userId) {
        Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
                        .tableName(DynamoDbTableNames.mainTable(properties))
                        .key(Map.of(
                                DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.roomPk(roomId)),
                                DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.memberSk(userId))
                        ))
                        .build())
                .item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toDomain(item));
    }

    @Override
    public RoomMember save(RoomMember roomMember) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(DynamoDbTableNames.mainTable(properties))
                .item(Map.of(
                        DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.roomPk(roomMember.roomId())),
                        DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.memberSk(roomMember.userId())),
                        ROOM_ID, AttributeValue.fromS(roomMember.roomId().value()),
                        USER_ID, AttributeValue.fromS(roomMember.userId().value()),
                        ROLE, AttributeValue.fromS(roomMember.role().name()),
                        STATUS, AttributeValue.fromS(roomMember.status().name()),
                        JOINED_AT, AttributeValue.fromS(roomMember.joinedAt().toString())
                ))
                .build());
        return roomMember;
    }

    private RoomMember toDomain(Map<String, AttributeValue> item) {
        return new RoomMember(
                new RoomId(item.get(ROOM_ID).s()),
                new UserId(item.get(USER_ID).s()),
                RoomMemberRole.valueOf(item.get(ROLE).s()),
                RoomMemberStatus.valueOf(item.get(STATUS).s()),
                Instant.parse(item.get(JOINED_AT).s())
        );
    }
}
