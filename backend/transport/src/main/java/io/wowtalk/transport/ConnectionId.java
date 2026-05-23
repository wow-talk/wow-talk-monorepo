package io.wowtalk.transport;

import java.util.UUID;

public record ConnectionId(String value) {

    public ConnectionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("연결 ID는 비어 있을 수 없습니다.");
        }
    }

    public static ConnectionId newId() {
        return new ConnectionId(UUID.randomUUID().toString());
    }
}
