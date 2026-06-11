package io.wowtalk.dynamodb.realtime;

import io.wowtalk.dynamodb.config.DynamoDbProperties;
import io.wowtalk.dynamodb.repository.DynamoDbKeys;
import io.wowtalk.dynamodb.support.DynamoDbTableNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Component
@ConditionalOnProperty(prefix = "wowtalk.dynamodb", name = {"enabled", "initialize-schema"}, havingValue = "true")
public class RoomEventDynamoDbTableInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RoomEventDynamoDbTableInitializer.class);

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;

    public RoomEventDynamoDbTableInitializer(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        createTable(DynamoDbTableNames.mainTable(properties));
        createTable(DynamoDbTableNames.roomEventsTable(properties));
    }

    private void createTable(String tableName) {
        try {
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(tableName)
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName(DynamoDbKeys.PK)
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName(DynamoDbKeys.SK)
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName(DynamoDbKeys.PK)
                                    .keyType(KeyType.HASH)
                                    .build(),
                            KeySchemaElement.builder()
                                    .attributeName(DynamoDbKeys.SK)
                                    .keyType(KeyType.RANGE)
                                    .build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
            log.info("DynamoDB table created: {}", tableName);
        } catch (ResourceInUseException exception) {
            log.info("DynamoDB table already exists: {}", tableName);
        }
    }
}
