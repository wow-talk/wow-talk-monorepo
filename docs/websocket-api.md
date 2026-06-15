# WebSocket API

## 목적
- 프론트엔드와 백엔드가 동일한 메시지 계약으로 통신한다.
- 현재는 `WebSocket` 구현을 먼저 사용한다.
- 이후 `Raw TCP`를 추가하더라도 메시지 의미와 타입은 최대한 동일하게 유지한다.

## 연결 주소
```text
ws://localhost:8080/ws/chat?roomId={roomId}&sessionId={sessionId}
```

신규 클라이언트는 `userId`를 함께 보낸다.

```text
ws://localhost:8080/ws/chat?roomId={roomId}&connectionId={connectionId}&sessionId={sessionId}&userId={userId}
```

v1 서버 응답을 받고 싶은 클라이언트는 `protocolVersion=1`을 추가한다.

```text
ws://localhost:8080/ws/chat?roomId={roomId}&connectionId={connectionId}&sessionId={sessionId}&userId={userId}&protocolVersion=1
```

예시:
```text
ws://localhost:8080/ws/chat?roomId=room-1&connectionId=conn-1&sessionId=conn-1&userId=guest-1&protocolVersion=1
```

## 연결 파라미터
- `roomId`: 채팅방 식별자
- `connectionId`: 현재 WebSocket 접속 식별자. 없으면 서버가 생성해서 `CONNECTED` 응답으로 반환한다.
- `sessionId`: 기존 클라이언트 호환용 세션 식별자. 장기적으로 `connectionId`로 대체한다.
- `userId`: 발신자 사용자 식별자. 없으면 legacy 호환을 위해 서버가 임시 guest user를 생성한다.
- `protocolVersion`: `1`이면 서버 -> 클라이언트 메시지를 v1 envelope로 내려준다. 없으면 legacy 응답을 유지한다.

## 클라이언트 -> 서버
현재는 legacy `SEND_MESSAGE`와 v1 `CHAT_SEND`를 함께 지원한다.

### Legacy SEND_MESSAGE

```json
{
  "type": "SEND_MESSAGE",
  "payload": "안녕하세요"
}
```

### v1 CHAT_SEND

```json
{
  "version": 1,
  "type": "CHAT_SEND",
  "requestId": "req-1",
  "roomId": "room-1",
  "payload": {
    "text": "안녕하세요"
  }
}
```

필드 설명:
- `type`: 메시지 타입
- `requestId`: v1 요청 추적 ID. 서버 응답에 보존될 수 있다.
- `payload`: legacy는 문자열, v1은 `{ "text": "..." }` 객체

## 서버 -> 클라이언트

`protocolVersion`이 없으면 legacy 응답을 내려준다.

`protocolVersion=1`이면 v1 envelope를 내려준다.

### v1 연결 성공

```json
{
  "version": 1,
  "type": "CONNECTED",
  "eventId": "evt-1",
  "requestId": null,
  "roomId": "room-1",
  "occurredAt": "2026-05-24T00:00:00Z",
  "payload": {
    "connectionId": "conn-1",
    "sessionId": "conn-1",
    "userId": "guest-1"
  }
}
```

### v1 채팅 메시지

```json
{
  "version": 1,
  "type": "CHAT_MESSAGE_CREATED",
  "eventId": "evt-2",
  "requestId": "req-1",
  "roomId": "room-1",
  "occurredAt": "2026-05-24T00:00:01Z",
  "payload": {
    "messageId": "7fdce1d7-8d0d-4f2f-9fa0-75f3df81d3d2",
    "connectionId": "conn-1",
    "sessionId": "conn-1",
    "senderUserId": "guest-1",
    "text": "안녕하세요"
  }
}
```

### v1 에러

가능한 경우 서버는 실패한 요청의 `requestId`와 `roomId`를 ERROR envelope에 포함한다.

```json
{
  "version": 1,
  "type": "ERROR",
  "eventId": "evt-error-1",
  "requestId": "req-1",
  "roomId": "room-1",
  "occurredAt": "2026-05-24T00:00:01Z",
  "payload": {
    "code": "INVALID_CHAT_MESSAGE",
    "message": "메시지 내용이 올바르지 않습니다."
  }
}
```

### Legacy 연결 성공
```json
{
  "type": "CONNECTED",
  "roomId": "room-1",
  "connectionId": "conn-1",
  "sessionId": "conn-1",
  "messageId": null,
  "senderUserId": null,
  "payload": "웹소켓 연결이 완료되었습니다.",
  "sentAt": null,
  "code": null,
  "message": null
}
```

### Legacy 채팅 메시지
```json
{
  "type": "CHAT_MESSAGE",
  "roomId": "room-1",
  "connectionId": "conn-1",
  "sessionId": "conn-1",
  "messageId": "7fdce1d7-8d0d-4f2f-9fa0-75f3df81d3d2",
  "senderUserId": "guest-1",
  "payload": "안녕하세요",
  "sentAt": "2026-04-24T12:00:00Z",
  "code": null,
  "message": null
}
```

### Legacy 에러
```json
{
  "type": "ERROR",
  "roomId": null,
  "connectionId": null,
  "sessionId": null,
  "messageId": null,
  "senderUserId": null,
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
- `ROOM_NOT_FOUND`
- `USER_NOT_FOUND`
- `TRANSPORT_MODE_MISMATCH`

## 프론트엔드 테스트 순서
1. 서버 실행: `./gradlew :apps:api:bootRun`
2. 브라우저에서 `http://localhost:8080/ws-test.html` 접속
3. 같은 `roomId`, 다른 `sessionId`로 두 탭 이상 연결
4. JSON 메시지 전송
5. 같은 방에만 `CHAT_MESSAGE`가 오는지 확인

## 비고
- 현재 로컬 프로필은 DynamoDB Local 기반 저장소를 사용한다. 운영 목표 저장소도 DynamoDB다.
- 이 문서는 현재 구현된 WebSocket 계약을 설명한다.
- 장기 프로토콜 방향은 [realtime-protocol-v1.md](./realtime-protocol-v1.md)를 기준으로 한다.
- ECS API task 3대 이상 scale-out 방향은 [realtime-scaleout.md](./realtime-scaleout.md)를 기준으로 한다.
- 이후 `Raw TCP`를 추가하더라도 메시지 의미와 유스케이스는 최대한 동일하게 유지한다.
