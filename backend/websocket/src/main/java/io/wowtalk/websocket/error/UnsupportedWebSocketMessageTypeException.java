package io.wowtalk.websocket.error;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;

/**
 * WebSocket adapter가 아직 지원하지 않는 메시지 타입 또는 프로토콜 버전을 받았을 때 발생한다.
 *
 * <p>클라이언트 계약 전환 기간에는 legacy 메시지와 protocol v1 envelope를 함께 받기 때문에,
 * 지원 범위를 벗어난 입력은 명확한 WebSocket 오류로 분리한다.
 */
public class UnsupportedWebSocketMessageTypeException extends WowTalkException implements WebSocketErrorContext {

    private final String requestId;
    private final String roomId;

    public UnsupportedWebSocketMessageTypeException() {
        this(null, null);
    }

    public UnsupportedWebSocketMessageTypeException(String requestId, String roomId) {
        super(ErrorCode.UNSUPPORTED_MESSAGE_TYPE);
        this.requestId = requestId;
        this.roomId = roomId;
    }

    @Override
    public String requestId() {
        return requestId;
    }

    @Override
    public String roomId() {
        return roomId;
    }
}
