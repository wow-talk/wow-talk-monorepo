# Realtime Protocol v1

## 목적

이 문서는 Wow Talk의 실시간 메시지 계약을 정의한다.

현재 브라우저 클라이언트는 WebSocket으로 접속하지만, 서버 내부 구조는 WebSocket에 종속되지 않아야 한다. 같은 의미의 command/event를 나중에 Raw TCP 또는 다른 transport에서도 재사용할 수 있어야 한다.

## 계층

```txt
Client protocol
  JSON over WebSocket

Transport adapter
  WebSocket inbound/outbound 변환

Application protocol
  RealtimeCommand / RealtimeEvent

Domain
  Chat, Room, User, Game
```

## Envelope

모든 클라이언트 -> 서버 메시지는 envelope를 가진다.

```json
{
  "version": 1,
  "type": "CHAT_SEND",
  "requestId": "01HZ0000000000000000000000",
  "roomId": "lobby",
  "payload": {}
}
```

모든 서버 -> 클라이언트 메시지도 envelope를 가진다.

```json
{
  "version": 1,
  "type": "CHAT_MESSAGE_CREATED",
  "eventId": "01HZ0000000000000000000001",
  "requestId": "01HZ0000000000000000000000",
  "roomId": "lobby",
  "occurredAt": "2026-05-23T13:00:00Z",
  "payload": {}
}
```

## 공통 필드

### version

프로토콜 버전이다. 초기값은 `1`.

### type

메시지 타입이다. command와 event를 명확히 구분한다.

```txt
Client -> Server: *_COMMAND 성격
Server -> Client: *_EVENT 성격
```

실제 문자열은 짧고 명확하게 유지한다.

### requestId

클라이언트 요청 추적용 ID다.

- 클라이언트가 생성한다.
- 서버 응답/에러에 그대로 포함한다.
- 중복 요청 방지와 디버깅에 사용한다.

### eventId

서버 이벤트 식별자다.

- 서버가 생성한다.
- 클라이언트 재처리 방지에 사용한다.

### roomId

방 식별자다.

### occurredAt

서버 기준 이벤트 발생 시각이다.

## Client -> Server

### HELLO

연결 직후 클라이언트 정보를 서버에 알린다.

```json
{
  "version": 1,
  "type": "HELLO",
  "requestId": "req-1",
  "roomId": "lobby",
  "payload": {
    "clientType": "WEB",
    "displayName": "guest"
  }
}
```

초기에는 WebSocket query string으로 `roomId`, `sessionId`를 받더라도, 장기적으로는 `HELLO` 메시지로 connection handshake를 명시한다.

### CHAT_SEND

채팅 메시지를 보낸다.

현재 WebSocket 구현에서 지원한다. legacy `SEND_MESSAGE`와 병행된다.

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

제약:

- `text`는 blank일 수 없다.
- 길이 제한이 필요하다.
- 추후 rich payload가 필요하면 `messageType`을 추가한다.

### PING

애플리케이션 레벨 heartbeat다.

아직 구현하지 않았다.

```json
{
  "version": 1,
  "type": "PING",
  "requestId": "req-3",
  "roomId": "lobby",
  "payload": {}
}
```

WebSocket protocol ping/pong과 별개로, 클라이언트 상태 표시와 latency 측정에 사용한다.

## Server -> Client

### CONNECTED

연결이 수락되었음을 알린다.

```json
{
  "version": 1,
  "type": "CONNECTED",
  "eventId": "evt-1",
  "requestId": "req-1",
  "roomId": "lobby",
  "occurredAt": "2026-05-23T13:00:00Z",
  "payload": {
    "connectionId": "conn-1",
    "userId": "guest-1"
  }
}
```

### CHAT_MESSAGE_CREATED

채팅 메시지가 생성되었음을 알린다.

```json
{
  "version": 1,
  "type": "CHAT_MESSAGE_CREATED",
  "eventId": "evt-2",
  "requestId": "req-2",
  "roomId": "lobby",
  "occurredAt": "2026-05-23T13:00:01Z",
  "payload": {
    "messageId": "msg-1",
    "senderUserId": "guest-1",
    "text": "안녕하세요"
  }
}
```

### ERROR

요청 처리 실패를 알린다.

```json
{
  "version": 1,
  "type": "ERROR",
  "eventId": "evt-error-1",
  "requestId": "req-2",
  "roomId": "lobby",
  "occurredAt": "2026-05-23T13:00:01Z",
  "payload": {
    "code": "INVALID_CHAT_MESSAGE",
    "message": "메시지 내용은 비어 있을 수 없습니다."
  }
}
```

## 게임 이벤트 확장

게임은 채팅과 같은 realtime envelope를 쓰되, type과 payload를 분리한다.

예상 타입:

```txt
GAME_JOIN
GAME_LEAVE
GAME_START
GAME_MISSION_ASSIGNED
GAME_ACTION
GAME_STATE_PATCH
GAME_SYSTEM_EVENT
```

예:

```json
{
  "version": 1,
  "type": "GAME_ACTION",
  "requestId": "req-game-1",
  "roomId": "game-1",
  "payload": {
    "actionType": "MOVE",
    "x": 10,
    "y": 12
  }
}
```

채팅 기반 게임에서는 채팅 메시지 자체가 게임 입력이 될 수 있다.

예:

```json
{
  "version": 1,
  "type": "CHAT_SEND",
  "requestId": "req-chat-1",
  "roomId": "game-1",
  "payload": {
    "text": "전기실 완료"
  }
}
```

서버는 해당 메시지를 일반 채팅으로 저장하면서, 현재 room의 game rule에 따라 `GAME_ACTION` 또는 `GAME_STATE_PATCH`를 추가로 발생시킬 수 있다.

```json
{
  "version": 1,
  "type": "GAME_ACTION_ACCEPTED",
  "eventId": "evt-game-1",
  "requestId": "req-chat-1",
  "roomId": "game-1",
  "occurredAt": "2026-05-23T13:00:02Z",
  "payload": {
    "actionType": "MISSION_TEXT_SUBMITTED",
    "userId": "guest-1",
    "missionId": "mission-1"
  }
}
```

이 원칙 때문에 protocol은 처음부터 `requestId`를 유지한다. 하나의 채팅 입력이 여러 서버 이벤트를 만들 수 있기 때문이다.

## 현재 구현과의 차이

현재 구현:

```json
{
  "type": "SEND_MESSAGE",
  "payload": "안녕하세요"
}
```

목표 구현:

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

따라서 다음 구현 단계에서는 기존 메시지를 바로 제거하지 말고, v1 envelope를 추가 지원한 뒤 프론트와 맞춰 전환한다.

## 구현 순서

1. protocol DTO 추가
2. 현재 `SEND_MESSAGE`와 v1 `CHAT_SEND`를 동시에 지원
3. outbound message에 `version`, `eventId`, `requestId` 추가
4. 프론트 전환
5. legacy `SEND_MESSAGE` 제거 여부 결정
