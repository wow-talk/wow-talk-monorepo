# WebSocket API

## 목적
- 프론트엔드와 백엔드가 동일한 메시지 계약으로 통신한다.
- 현재는 `WebSocket` 구현을 먼저 사용한다.
- 이후 `Raw TCP`를 추가하더라도 메시지 의미와 타입은 최대한 동일하게 유지한다.

## 연결 주소
```text
ws://localhost:8080/ws/chat?roomId={roomId}&sessionId={sessionId}
```

예시:
```text
ws://localhost:8080/ws/chat?roomId=room-1&sessionId=user-1
```

## 연결 파라미터
- `roomId`: 채팅방 식별자
- `sessionId`: 클라이언트 세션 식별자

## 클라이언트 -> 서버
현재 지원 타입은 `SEND_MESSAGE` 하나다.

```json
{
  "type": "SEND_MESSAGE",
  "payload": "안녕하세요"
}
```

필드 설명:
- `type`: 메시지 타입
- `payload`: 실제 채팅 내용

## 서버 -> 클라이언트

### 1. 연결 성공
```json
{
  "type": "CONNECTED",
  "roomId": "room-1",
  "sessionId": "user-1",
  "payload": "웹소켓 연결이 완료되었습니다.",
  "sentAt": null,
  "code": null,
  "message": null
}
```

### 2. 채팅 메시지
```json
{
  "type": "CHAT_MESSAGE",
  "roomId": "room-1",
  "sessionId": "user-1",
  "payload": "안녕하세요",
  "sentAt": "2026-04-24T12:00:00Z",
  "code": null,
  "message": null
}
```

### 3. 에러
```json
{
  "type": "ERROR",
  "roomId": null,
  "sessionId": null,
  "payload": null,
  "sentAt": null,
  "code": "INVALID_WEBSOCKET_MESSAGE_FORMAT",
  "message": "웹소켓 메시지 형식이 올바르지 않습니다."
}
```

## 현재 에러 코드
- `WEBSOCKET_CONNECTION_INVALID`
- `INVALID_WEBSOCKET_MESSAGE_FORMAT`
- `UNSUPPORTED_MESSAGE_TYPE`
- `INVALID_CHAT_MESSAGE`
- `CHANNEL_NOT_FOUND`
- `TRANSPORT_MODE_MISMATCH`

## 프론트엔드 테스트 순서
1. 서버 실행: `./gradlew :apps:api:bootRun`
2. 브라우저에서 `http://localhost:8080/ws-test.html` 접속
3. 같은 `roomId`, 다른 `sessionId`로 두 탭 이상 연결
4. JSON 메시지 전송
5. 같은 방에만 `CHAT_MESSAGE`가 오는지 확인

## 비고
- 현재 채널/메시지는 메모리 기반이다.
- 서버 재시작 시 데이터는 유지되지 않는다.
- 이 계약은 이후 `Raw TCP` 구현에서도 재사용 가능한 기준 문서다.
