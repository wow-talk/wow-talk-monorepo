package io.wowtalk.common.error;

public enum ErrorCode {
    CHANNEL_NOT_FOUND("채널을 찾을 수 없습니다."),
    TRANSPORT_MODE_MISMATCH("채널의 전송 방식과 요청한 전송 방식이 다릅니다."),
    INVALID_CHAT_MESSAGE("메시지 내용이 올바르지 않습니다."),
    WEBSOCKET_CONNECTION_INVALID("웹소켓 연결 정보가 올바르지 않습니다."),
    INVALID_WEBSOCKET_MESSAGE_FORMAT("웹소켓 메시지 형식이 올바르지 않습니다."),
    UNSUPPORTED_MESSAGE_TYPE("지원하지 않는 메시지 타입입니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
