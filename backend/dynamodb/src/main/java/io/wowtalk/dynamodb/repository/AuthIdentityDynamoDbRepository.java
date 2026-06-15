package io.wowtalk.dynamodb.repository;

import io.wowtalk.dynamodb.config.DynamoDbProperties;
import io.wowtalk.dynamodb.support.DynamoDbTableNames;
import io.wowtalk.user.domain.AuthIdentity;
import io.wowtalk.user.domain.AuthIdentityId;
import io.wowtalk.user.domain.AuthProvider;
import io.wowtalk.user.domain.UserId;
import io.wowtalk.user.repository.AuthIdentityRepository;
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
 * AuthIdentity를 DynamoDB main table에 저장하는 adapter 구현체다.
 *
 * <p>provider와 providerSubject 조합을 PK로 사용해 로그인/guest credential 검증 시 내부 userId를
 * 빠르게 찾을 수 있게 한다.
 */
@Repository
@ConditionalOnProperty(prefix = "wowtalk.dynamodb", name = "enabled", havingValue = "true")
public class AuthIdentityDynamoDbRepository implements AuthIdentityRepository {

    private static final String AUTH_IDENTITY_ID = "authIdentityId";
    private static final String USER_ID = "userId";
    private static final String PROVIDER = "provider";
    private static final String PROVIDER_SUBJECT = "providerSubject";
    private static final String CREATED_AT = "createdAt";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;

    public AuthIdentityDynamoDbRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public Optional<AuthIdentity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject) {
        Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
                        .tableName(DynamoDbTableNames.mainTable(properties))
                        .key(Map.of(
                                DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.authIdentityPk(provider.name(), providerSubject)),
                                DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.authIdentitySk())
                        ))
                        .build())
                .item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toDomain(item));
    }

    @Override
    public AuthIdentity save(AuthIdentity authIdentity) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(DynamoDbTableNames.mainTable(properties))
                .item(Map.of(
                        DynamoDbKeys.PK, AttributeValue.fromS(DynamoDbKeys.authIdentityPk(
                                authIdentity.provider().name(),
                                authIdentity.providerSubject()
                        )),
                        DynamoDbKeys.SK, AttributeValue.fromS(DynamoDbKeys.authIdentitySk()),
                        AUTH_IDENTITY_ID, AttributeValue.fromS(authIdentity.authIdentityId().value()),
                        USER_ID, AttributeValue.fromS(authIdentity.userId().value()),
                        PROVIDER, AttributeValue.fromS(authIdentity.provider().name()),
                        PROVIDER_SUBJECT, AttributeValue.fromS(authIdentity.providerSubject()),
                        CREATED_AT, AttributeValue.fromS(authIdentity.createdAt().toString())
                ))
                .build());
        return authIdentity;
    }

    private AuthIdentity toDomain(Map<String, AttributeValue> item) {
        return new AuthIdentity(
                new AuthIdentityId(item.get(AUTH_IDENTITY_ID).s()),
                new UserId(item.get(USER_ID).s()),
                AuthProvider.valueOf(item.get(PROVIDER).s()),
                item.get(PROVIDER_SUBJECT).s(),
                Instant.parse(item.get(CREATED_AT).s())
        );
    }
}
