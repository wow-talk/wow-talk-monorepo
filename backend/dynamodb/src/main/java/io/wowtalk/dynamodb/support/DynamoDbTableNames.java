package io.wowtalk.dynamodb.support;

import io.wowtalk.dynamodb.config.DynamoDbProperties;

public final class DynamoDbTableNames {

    private DynamoDbTableNames() {
    }

    public static String mainTable(DynamoDbProperties properties) {
        return properties.resolvedMainTable();
    }

    public static String roomEventsTable(DynamoDbProperties properties) {
        return properties.roomEventsTable();
    }
}
