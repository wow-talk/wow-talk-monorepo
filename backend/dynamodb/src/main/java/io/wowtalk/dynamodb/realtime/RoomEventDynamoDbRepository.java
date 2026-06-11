package io.wowtalk.dynamodb.realtime;

import io.wowtalk.dynamodb.config.DynamoDbProperties;
import io.wowtalk.realtime.domain.EventId;
import io.wowtalk.realtime.domain.RoomEvent;
import io.wowtalk.realtime.domain.RoomEventType;
import io.wowtalk.realtime.repository.RoomEventRepository;
import io.wowtalk.transport.RoomId;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

@Repository
@ConditionalOnProperty(prefix = "wowtalk.dynamodb", name = "enabled", havingValue = "true")
public class RoomEventDynamoDbRepository implements RoomEventRepository {

    /*
     * Room events are stored in a dedicated stream table so game/system events can grow
     * independently from the MVP chat message table.
     */
    static final String PK = "pk";
    static final String SK = "sk";

    private static final String ROOM_PREFIX = "ROOM#";
    private static final String EVENT_PREFIX = "EVT#";
    private static final String EVENT_ID = "eventId";
    private static final String ROOM_ID = "roomId";
    private static final String EVENT_TYPE = "eventType";
    private static final String ACTOR_USER_ID = "actorUserId";
    private static final String PAYLOAD = "payload";
    private static final String OCCURRED_AT = "occurredAt";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;

    public RoomEventDynamoDbRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public RoomEvent save(RoomEvent roomEvent) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(properties.roomEventsTable())
                .item(toItem(roomEvent))
                .build());
        return roomEvent;
    }

    @Override
    public List<RoomEvent> findRecentByRoomId(RoomId roomId, int limit) {
        Map<String, AttributeValue> values = Map.of(
                ":pk", AttributeValue.fromS(partitionKey(roomId)),
                ":skPrefix", AttributeValue.fromS(EVENT_PREFIX)
        );

        return dynamoDbClient.query(QueryRequest.builder()
                        .tableName(properties.roomEventsTable())
                        .keyConditionExpression("pk = :pk AND begins_with(sk, :skPrefix)")
                        .expressionAttributeValues(values)
                        .scanIndexForward(false)
                        .limit(limit)
                        .build())
                .items()
                .stream()
                .map(this::toDomain)
                .sorted(Comparator.comparing(RoomEvent::occurredAt))
                .toList();
    }

    private Map<String, AttributeValue> toItem(RoomEvent roomEvent) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(PK, AttributeValue.fromS(partitionKey(roomEvent.roomId())));
        item.put(SK, AttributeValue.fromS(sortKey(roomEvent)));
        item.put(EVENT_ID, AttributeValue.fromS(roomEvent.eventId().value()));
        item.put(ROOM_ID, AttributeValue.fromS(roomEvent.roomId().value()));
        item.put(EVENT_TYPE, AttributeValue.fromS(roomEvent.eventType().name()));
        item.put(PAYLOAD, AttributeValue.fromS(roomEvent.payload()));
        item.put(OCCURRED_AT, AttributeValue.fromS(roomEvent.occurredAt().toString()));
        if (roomEvent.actorUserId() != null && !roomEvent.actorUserId().isBlank()) {
            item.put(ACTOR_USER_ID, AttributeValue.fromS(roomEvent.actorUserId()));
        }
        return item;
    }

    private RoomEvent toDomain(Map<String, AttributeValue> item) {
        AttributeValue actorUserId = item.get(ACTOR_USER_ID);
        return new RoomEvent(
                new EventId(item.get(EVENT_ID).s()),
                new RoomId(item.get(ROOM_ID).s()),
                RoomEventType.valueOf(item.get(EVENT_TYPE).s()),
                actorUserId == null ? null : actorUserId.s(),
                item.get(PAYLOAD).s(),
                Instant.parse(item.get(OCCURRED_AT).s())
        );
    }

    private String partitionKey(RoomId roomId) {
        return ROOM_PREFIX + roomId.value();
    }

    private String sortKey(RoomEvent roomEvent) {
        return EVENT_PREFIX + roomEvent.occurredAt().toEpochMilli() + "#" + roomEvent.eventId().value();
    }
}
