package io.wowtalk.transport;

public record RoomId(String value) {

    public RoomId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("채팅방 ID는 비어 있을 수 없습니다.");
        }
    }
}
