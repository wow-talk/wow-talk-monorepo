package io.wowtalk.transport;

public record SessionId(String value) {

    public SessionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("세션 ID는 비어 있을 수 없습니다.");
        }
    }
}
