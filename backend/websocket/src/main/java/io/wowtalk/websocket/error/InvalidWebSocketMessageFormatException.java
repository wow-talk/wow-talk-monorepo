package io.wowtalk.websocket.error;

import io.wowtalk.common.error.ErrorCode;
import io.wowtalk.common.error.WowTalkException;

/**
 * WebSocket으로 들어온 클라이언트 메시지가 서버가 이해하는 JSON 계약을 만족하지 않을 때 발생한다.
 *
 * <p>이 예외는 WebSocket adapter 내부의 프로토콜 파싱 실패를 표현한다. core 도메인 규칙이 아니라
 * transport 구현체의 입력 변환 실패이므로 websocket 모듈에 둔다.
 */
public class InvalidWebSocketMessageFormatException extends WowTalkException {

    public InvalidWebSocketMessageFormatException() {
        super(ErrorCode.INVALID_WEBSOCKET_MESSAGE_FORMAT);
    }
}
