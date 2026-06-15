package io.wowtalk.websocket.error;

/**
 * WebSocket 메시지 파싱 중 확보한 요청 추적 정보를 오류 응답까지 전달하기 위한 context 계약이다.
 *
 * <p>잘못된 v1 메시지라도 requestId와 roomId를 읽을 수 있는 경우가 있다. 이 값은 클라이언트가
 * 실패한 요청과 ERROR envelope를 연결하는 데 사용한다.
 */
public interface WebSocketErrorContext {

    String requestId();

    String roomId();
}
