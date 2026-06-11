package io.wowtalk.dynamodb.repository;

import io.wowtalk.dynamodb.config.DynamoDbProperties;
import io.wowtalk.dynamodb.support.DynamoDbTableNames;
import io.wowtalk.user.domain.User;
import io.wowtalk.user.domain.UserId;
import io.wowtalk.user.domain.UserType;
import io.wowtalk.user.repository.UserRepository;
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
public class UserDynamoDbRepository implements UserRepository {

    private static final String USER_ID = "userId";
    private static final String USER_TYPE = "userType";
    private static final String DISPLAY_NAME = "displayName";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;

    public UserDynamoDbRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public Optional<User> findByUserId(UserId userId) {
        Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
                        .tableName(DynamoDbTableNames.mainTable(properties))
                        .key(Map.of(
                                DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.userPk(userId)),
                                DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.userSk())
                        ))
                        .build())
                .item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toDomain(item));
    }

    @Override
    public User save(User user) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(DynamoDbTableNames.mainTable(properties))
                .item(Map.of(
                        DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.userPk(user.userId())),
                        DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.userSk()),
                        USER_ID, AttributeValue.fromS(user.userId().value()),
                        USER_TYPE, AttributeValue.fromS(user.userType().name()),
                        DISPLAY_NAME, AttributeValue.fromS(user.displayName())
                ))
                .build());
        return user;
    }

    private User toDomain(Map<String, AttributeValue> item) {
        return new User(
                new UserId(item.get(USER_ID).s()),
                UserType.valueOf(item.get(USER_TYPE).s()),
                item.get(DISPLAY_NAME).s()
        );
    }
}
