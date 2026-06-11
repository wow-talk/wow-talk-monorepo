package io.wowtalk.dynamodb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wowtalk.dynamodb")
public record DynamoDbProperties(
        boolean enabled,
        String endpoint,
        String region,
        String mainTable,
        String roomEventsTable
) {

    public String resolvedRegion() {
        if (region == null || region.isBlank()) {
            return "ap-northeast-2";
        }
        return region;
    }

    public String resolvedMainTable() {
        if (mainTable == null || mainTable.isBlank()) {
            return "wowtalk-main";
        }
        return mainTable;
    }
}
