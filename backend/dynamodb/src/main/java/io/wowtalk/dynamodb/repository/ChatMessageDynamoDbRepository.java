package io.wowtalk.dynamodb.repository;

import io.wowtalk.dynamodb.config.DynamoDbProperties;
import io.wowtalk.dynamodb.support.DynamoDbTableNames;
import io.wowtalk.message.domain.ChatMessage;
import io.wowtalk.message.domain.MessageId;
import io.wowtalk.message.repository.ChatMessageRepository;
import io.wowtalk.transport.RoomId;
import io.wowtalk.transport.SessionId;
import io.wowtalk.user.domain.UserId;
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
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

@Repository
@ConditionalOnProperty(prefix = "wowtalk.dynamodb", name = "enabled", havingValue = "true")
public class ChatMessageDynamoDbRepository implements ChatMessageRepository {

    private static final String MESSAGE_ID = "messageId";
    private static final String ROOM_ID = "roomId";
    private static final String SESSION_ID = "sessionId";
    private static final String SENDER_USER_ID = "senderUserId";
    private static final String PAYLOAD = "payload";
    private static final String SENT_AT = "sentAt";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;

    public ChatMessageDynamoDbRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(DynamoDbTableNames.mainTable(properties))
                .item(toItem(chatMessage))
                .build());
        return chatMessage;
    }

    @Override
    public List<ChatMessage> findAll() {
        return dynamoDbClient.scan(ScanRequest.builder()
                        .tableName(DynamoDbTableNames.mainTable(properties))
                        .filterExpression("begins_with(sk, :skPrefix)")
                        .expressionAttributeValues(Map.of(":skPrefix", AttributeValue.fromS(DynamoDbKeys.MESSAGE_PREFIX)))
                        .build())
                .items()
                .stream()
                .map(this::toDomain)
                .sorted(Comparator.comparing(ChatMessage::sentAt))
                .toList();
    }

    @Override
    public List<ChatMessage> findRecentByRoomId(RoomId roomId, int limit) {
        return dynamoDbClient.query(QueryRequest.builder()
                        .tableName(DynamoDbTableNames.mainTable(properties))
                        .keyConditionExpression("pk = :pk AND begins_with(sk, :skPrefix)")
                        .expressionAttributeValues(Map.of(
                                ":pk", AttributeValue.fromS(DynamoDbKeys.roomPk(roomId)),
                                ":skPrefix", AttributeValue.fromS(DynamoDbKeys.MESSAGE_PREFIX)
                        ))
                        .scanIndexForward(false)
                        .limit(limit)
                        .build())
                .items()
                .stream()
                .map(this::toDomain)
                .sorted(Comparator.comparing(ChatMessage::sentAt))
                .toList();
    }

    private Map<String, AttributeValue> toItem(ChatMessage chatMessage) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.roomPk(chatMessage.roomId())));
        item.put(DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.messageSk(chatMessage)));
        item.put(MESSAGE_ID, AttributeValue.fromS(chatMessage.messageId().value()));
        item.put(ROOM_ID, AttributeValue.fromS(chatMessage.roomId().value()));
        item.put(SESSION_ID, AttributeValue.fromS(chatMessage.sessionId().value()));
        item.put(SENDER_USER_ID, AttributeValue.fromS(chatMessage.senderUserId().value()));
        item.put(PAYLOAD, AttributeValue.fromS(chatMessage.payload()));
        item.put(SENT_AT, AttributeValue.fromS(chatMessage.sentAt().toString()));
        return item;
    }

    private ChatMessage toDomain(Map<String, AttributeValue> item) {
        return new ChatMessage(
                new MessageId(item.get(MESSAGE_ID).s()),
                new RoomId(item.get(ROOM_ID).s()),
                new SessionId(item.get(SESSION_ID).s()),
                new UserId(item.get(SENDER_USER_ID).s()),
                item.get(PAYLOAD).s(),
                Instant.parse(item.get(SENT_AT).s())
        );
    }
}
