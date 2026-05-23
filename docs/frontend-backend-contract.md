# Frontend Backend Contract

## 목적

백엔드 구조를 확장하면서 프론트엔드가 어떤 순서로 따라오면 되는지 정리한다.

원칙:

- 백엔드는 기존 WebSocket MVP를 깨지 않고 확장한다.
- 새 protocol은 legacy contract와 일정 기간 병행한다.
- 프론트가 한 번에 모든 변경을 따라오지 않아도 되게 한다.

## 현재 프론트 계약

### REST

채널 보장:

```http
POST /api/v1/channels
```

요청:

```json
{
  "roomId": "lobby",
  "transportMode": "WEBSOCKET"
}
```

최근 메시지:

```http
GET /api/v1/channels/{roomId}/messages?limit=50
```

게스트 사용자 발급:

```http
POST /api/v1/guests
```

### WebSocket

연결:

```txt
ws://localhost:8080/ws/chat?roomId={roomId}&sessionId={sessionId}
```

신규 클라이언트는 `userId`를 추가한다.

```txt
ws://localhost:8080/ws/chat?roomId={roomId}&sessionId={sessionId}&userId={userId}
```

송신:

```json
{
  "type": "SEND_MESSAGE",
  "payload": "안녕하세요"
}
```

수신:

```json
{
  "type": "CHAT_MESSAGE",
  "roomId": "lobby",
  "sessionId": "conn-1",
  "messageId": "7fdce1d7-8d0d-4f2f-9fa0-75f3df81d3d2",
  "senderUserId": "guest-1",
  "payload": "안녕하세요",
  "sentAt": "2026-05-23T13:00:00Z"
}
```

## 변경 방향

### Step 1. 응답에 ID 추가

프론트 영향이 작다.

추가 완료:

```txt
messageId
senderUserId
```

추가 예정:

```txt
connectionId
```

기존 필드는 일정 기간 유지한다.

### Step 2. Guest user API 추가

프론트는 앱 시작 또는 방 입장 전에 guest user를 만든다.

```http
POST /api/v1/guests
```

응답:

```json
{
  "userId": "guest-1",
  "displayName": "guest",
  "userType": "GUEST"
}
```

프론트 저장소는 다음 값을 가진다.

```txt
userId
displayName
sessionId
```

### Step 3. WebSocket 연결 파라미터 변경

기존:

```txt
roomId + sessionId
```

목표:

```txt
roomId + connectionId
```

그리고 WebSocket 연결 후 `HELLO`를 보낸다.

```json
{
  "version": 1,
  "type": "HELLO",
  "requestId": "req-1",
  "roomId": "lobby",
  "payload": {
    "userId": "guest-1",
    "displayName": "guest"
  }
}
```

### Step 4. Protocol envelope 전환

legacy:

```json
{
  "type": "SEND_MESSAGE",
  "payload": "안녕하세요"
}
```

v1:

```json
{
  "version": 1,
  "type": "CHAT_SEND",
  "requestId": "req-2",
  "roomId": "lobby",
  "payload": {
    "text": "안녕하세요"
  }
}
```

서버는 전환 기간 동안 둘 다 받는다.

## 프론트 구현 영향도

### 낮음

- message response에 `messageId` 추가
- display용 `senderUserId` 추가
- error payload 필드 추가

### 중간

- guest user 생성 API 호출
- 기존 session store에 userId/connectionId 추가
- WebSocket 연결 파라미터 변경

### 높음

- protocol envelope v1 전환
- 게임 이벤트 UI 처리
- social login 연결

## 게임 이벤트 UI 계약

게임 이벤트는 채팅 메시지와 같은 stream으로 내려올 수 있다.

프론트는 event type에 따라 렌더링을 분기한다.

```txt
CHAT_MESSAGE_CREATED -> 채팅 버블
GAME_MISSION_ASSIGNED -> 미션 UI 또는 시스템 메시지
GAME_ACTION_ACCEPTED -> 상태 업데이트
GAME_STATE_PATCH -> 게임 화면 업데이트
ERROR -> 에러 토스트 또는 시스템 메시지
```

채팅 입력이 게임 액션을 유발할 수 있으므로, 프론트는 하나의 `CHAT_SEND` 이후 여러 event가 내려올 수 있다는 전제를 가져야 한다.

## 백엔드 호환성 원칙

- 프론트가 전환하기 전까지 legacy `SEND_MESSAGE`를 유지한다.
- 새 필드는 optional로 시작한다.
- breaking change는 문서에 먼저 기록한다.
- WebSocket event type은 추가는 가능하지만 의미 변경은 피한다.

## 프론트에 먼저 공유할 것

1. guest user API가 생길 예정
2. `sessionId`는 장기적으로 `connectionId`가 된다
3. user identity는 `userId`로 분리된다
4. 메시지는 `messageId`와 `senderUserId`를 가진다
5. WebSocket payload는 envelope v1으로 바뀔 예정이다
6. 게임 이벤트는 채팅 stream 위에 같이 내려올 수 있다
