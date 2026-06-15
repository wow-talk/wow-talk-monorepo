# Frontend Integration Handoff

## 목적

프론트엔드가 현재 백엔드에 붙기 위한 최소 연동 순서를 정리한다.

백엔드는 legacy WebSocket과 protocol v1을 함께 지원한다. 신규 프론트는 가능하면 v1 흐름을 사용한다.

## 기본 주소

```txt
API base: http://localhost:8080
WS base : ws://localhost:8080
Web app : http://localhost:3000
```

## 권장 진입 흐름

### 1. 게스트 사용자 생성

```http
POST /api/v1/guests
Content-Type: application/json

{
  "displayName": "정균"
}
```

응답:

```json
{
  "userId": "guest-user-id",
  "userType": "GUEST",
  "displayName": "정균",
  "connectionId": "connection-id",
  "sessionId": "connection-id"
}
```

프론트는 아래 값을 저장한다.

```txt
userId
displayName
connectionId
sessionId
```

`sessionId`는 legacy 호환용이다. 현재 WebSocket 연결에는 아직 필요하므로 `connectionId`와 같은 값으로 보관한다.

### 2. 방 생성 또는 조회

```http
POST /api/v1/rooms
Content-Type: application/json

{
  "roomId": "lobby",
  "roomType": "CHAT",
  "maxMembers": 10
}
```

응답:

```json
{
  "roomId": "lobby",
  "roomType": "CHAT",
  "status": "WAITING",
  "maxMembers": 10,
  "createdAt": "2026-06-14T14:00:00Z"
}
```

기존 방은 아래로 조회한다.

```http
GET /api/v1/rooms/lobby
```

### 3. 방 참여

```http
POST /api/v1/rooms/lobby/members
Content-Type: application/json

{
  "userId": "guest-user-id"
}
```

응답:

```json
{
  "roomId": "lobby",
  "userId": "guest-user-id",
  "role": "MEMBER",
  "status": "ACTIVE",
  "joinedAt": "2026-06-14T14:00:01Z"
}
```

### 4. WebSocket 연결

신규 프론트는 `protocolVersion=1`을 붙인다.

```txt
ws://localhost:8080/ws/chat?roomId=lobby&connectionId={connectionId}&sessionId={sessionId}&userId={userId}&protocolVersion=1
```

연결 성공 이벤트:

```json
{
  "version": 1,
  "type": "CONNECTED",
  "eventId": "event-id",
  "requestId": null,
  "roomId": "lobby",
  "occurredAt": "2026-06-14T14:00:02Z",
  "payload": {
    "connectionId": "connection-id",
    "sessionId": "connection-id",
    "userId": "guest-user-id"
  }
}
```

### 5. 채팅 전송

```json
{
  "version": 1,
  "type": "CHAT_SEND",
  "requestId": "req-1",
  "roomId": "lobby",
  "payload": {
    "text": "안녕하세요"
  }
}
```

채팅 수신:

```json
{
  "version": 1,
  "type": "CHAT_MESSAGE_CREATED",
  "eventId": "event-id",
  "requestId": "req-1",
  "roomId": "lobby",
  "occurredAt": "2026-06-14T14:00:03Z",
  "payload": {
    "messageId": "message-id",
    "connectionId": "connection-id",
    "sessionId": "connection-id",
    "senderUserId": "guest-user-id",
    "text": "안녕하세요"
  }
}
```

### 6. 오류 응답

가능한 경우 v1 ERROR에는 실패한 요청의 `requestId`와 `roomId`가 포함된다.

```json
{
  "version": 1,
  "type": "ERROR",
  "eventId": "event-id",
  "requestId": "req-1",
  "roomId": "lobby",
  "occurredAt": "2026-06-14T14:00:04Z",
  "payload": {
    "code": "INVALID_CHAT_MESSAGE",
    "message": "메시지 내용이 올바르지 않습니다."
  }
}
```

## 프론트 구현 메모

- REST 응답은 Zod schema로 검증한다.
- 현재 legacy `sessionId`는 `connectionId`와 같은 값으로 저장해도 된다.
- 화면에서 본인 메시지 판정은 장기적으로 `senderUserId === currentUserId` 기준으로 바꾸는 것이 좋다.
- 현재 기존 프론트는 `sessionId` 기준으로 본인 메시지를 판정한다.
- 같은 Chrome 프로필의 여러 탭은 localStorage를 공유하므로, 테스트 시 일반 창 + 시크릿 창을 사용하면 다른 사용자처럼 확인하기 쉽다.

## 아직 미완료

- `HELLO` command
- `PING` command
- 메시지 최대 길이 정책
- `ConnectionSession` 영속 모델
- 친구/알림 API
