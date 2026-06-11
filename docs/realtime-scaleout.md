# Realtime Scale-out Design

## 목적

ECS에서 API 서버를 3대 이상 실행하는 상황을 기준으로 WebSocket 채팅과 게임 이벤트를 안정적으로 전달하기 위한 구조를 정리한다.

이 문서는 다음 질문에 답하기 위한 기준 문서다.

```txt
서버가 여러 대이면 같은 방의 WebSocket 메시지는 어떻게 모든 유저에게 전달되는가?
RDS 없이 DynamoDB를 사용할 때 채팅/게임 이벤트는 어떻게 저장하는가?
ECS 배포에서 백엔드는 어떤 상태를 메모리에 두면 안 되는가?
```

## 현재 구조

현재 WebSocket 연결은 각 API 서버의 JVM 메모리에 등록된다.

```txt
Client
-> API server
-> WebSocketSessionRegistry
```

`WebSocketSessionRegistry`는 현재 서버에 연결된 socket만 알고 있다.

서버가 1대이면 다음 흐름이 가능하다.

```txt
A user -> API-1
B user -> API-1

A sends message
-> API-1 saves message
-> API-1 broadcasts to room sessions
-> A and B receive message
```

하지만 서버가 3대이면 문제가 생긴다.

```txt
A user -> API-1
B user -> API-2
C user -> API-3

A sends message
-> API-1 only knows A socket
-> API-2 and API-3 do not receive the event
```

따라서 `WebSocketSessionRegistry`를 전체 방 상태로 보면 안 된다. 이 객체는 각 서버의 local connection registry로 제한해야 한다.

## 목표 구조

ECS API task는 3대 이상 실행될 수 있어야 한다.

```txt
Client
-> ALB
-> ECS API Task 1
-> ECS API Task 2
-> ECS API Task 3
```

각 API task는 자기에게 연결된 WebSocket만 관리한다.

서버 간 메시지 전파는 별도 realtime event channel을 통해 처리한다.

```txt
A user -> API-1
B user -> API-2
C user -> API-3

A sends message
-> API-1 stores event in DynamoDB
-> API-1 publishes room event
-> API-1, API-2, API-3 receive room event
-> each API task broadcasts to its local sockets
```

## 역할 분리

### ECS API task

담당:

```txt
REST API
WebSocket connection
message validation
local socket broadcast
```

두면 안 되는 것:

```txt
global room state
global game state
global user presence
message history as memory-only data
```

### DynamoDB

담당:

```txt
User
Room
RoomMember
ChatMessage
GameEvent
Room event stream
```

채팅과 게임 이벤트는 append-heavy이고 room 단위 시간순 조회가 중요하므로 DynamoDB와 잘 맞는다.

### Realtime event broker

담당:

```txt
server-to-server fan-out
room event publish/subscribe
multi-instance WebSocket broadcast
```

후보:

```txt
Redis Pub/Sub
Kafka
SNS/SQS
EventBridge
DynamoDB Streams
```

후보 비교:

| 후보 | 장점 | 단점 | 판단 |
| --- | --- | --- | --- |
| Redis Pub/Sub | WebSocket fan-out 구현이 단순하고 지연이 낮다. | ElastiCache 비용이 고정으로 발생한다. Pub/Sub 자체는 이벤트 저장소가 아니다. | 채팅 scale-out PoC 1순위 |
| Kafka / MSK | 이벤트 로그, 재처리, 대량 스트리밍에 강하다. | 운영 비용과 학습 난이도가 높다. 초기 채팅 MVP에는 무겁다. | 게임 replay/분석까지 커질 때 검토 |
| SNS/SQS | AWS managed이고 운영 부담이 낮다. | SQS는 작업 분배 큐라 모든 API task fan-out 구조가 복잡하다. | 비용/운영 우선이면 후보 |
| EventBridge | AWS 서비스 연동과 이벤트 라우팅이 좋다. | 낮은 지연의 채팅 broadcast에는 과할 수 있다. | 시스템 이벤트 라우팅에 적합 |
| DynamoDB Streams | DynamoDB 저장 이벤트와 자연스럽게 연결된다. | ECS task의 local WebSocket으로 즉시 fan-out하는 구조가 복잡하다. | 저장 후 후처리에는 좋지만 socket fan-out 단독 해법으로는 신중 |

초기 구현 난이도와 WebSocket fan-out 성격을 기준으로는 Redis Pub/Sub이 가장 단순하다. 비용을 낮춰야 하면 작은 ElastiCache 노드로 시작하거나 AWS managed event 방식을 별도 검토한다.

중요한 판단:

```txt
DynamoDB만으로 영속 저장소를 구성할 수는 있다.
하지만 DynamoDB는 서버 간 WebSocket fan-out broker가 아니다.
ECS API task 3대 이상에서는 별도 realtime event channel이 필요하다.
```

## DynamoDB 저장 모델 초안

채팅과 게임 이벤트를 같은 room event stream으로 저장한다.

테이블 후보:

```txt
wowtalk-room-events
```

키:

```txt
PK = ROOM#<roomId>
SK = EVT#<occurredAtEpochMillis>#<eventId>
```

채팅 메시지 예:

```json
{
  "pk": "ROOM#lobby",
  "sk": "EVT#1779540000000#evt-1",
  "eventType": "CHAT_MESSAGE_CREATED",
  "eventId": "evt-1",
  "messageId": "msg-1",
  "roomId": "lobby",
  "actorUserId": "user-1",
  "payload": {
    "text": "hello"
  },
  "occurredAt": "2026-05-30T10:00:00Z"
}
```

게임 이벤트 예:

```json
{
  "pk": "ROOM#lobby",
  "sk": "EVT#1779540001000#evt-2",
  "eventType": "PLAYER_READY_CHANGED",
  "eventId": "evt-2",
  "roomId": "lobby",
  "actorUserId": "user-2",
  "payload": {
    "ready": true
  },
  "occurredAt": "2026-05-30T10:00:01Z"
}
```

지원 access pattern:

```txt
방의 최근 이벤트 조회
방의 특정 시각 이후 이벤트 조회
방 입장 시 채팅/게임 이벤트 복원
```

## 백엔드 코드 변경 방향

현재 handler는 메시지 저장 후 즉시 local broadcast를 호출한다.

```txt
WebSocketChatHandler
-> ChatService.send()
-> WebSocket transport broadcast
```

scale-out을 위해 다음 구조로 변경한다.

```txt
WebSocketChatHandler
-> ChatService.send()
-> RealtimeEventPublisher.publish()

RealtimeEventSubscriber
-> receive room event
-> WebSocketChatTransport.broadcast()
```

기본 local profile에서는 local publisher 구현으로 기존 단일 서버 동작을 유지한다.

```txt
RealtimeEventPublisher
LocalRealtimeEventPublisher
```

multi-instance 검증 또는 운영 broker 모드에서는 Redis Pub/Sub adapter로 교체한다.

```txt
RedisRealtimeEventPublisher
RedisRealtimeEventSubscriber
```

설정:

```txt
wowtalk.realtime.broker=local
wowtalk.realtime.broker=redis
```

Redis 모드에서는 모든 API task가 같은 Redis channel pattern을 subscribe한다. 메시지를 받은 각 API task는 자신에게 연결된 local WebSocket 세션에만 broadcast한다.

## 로컬 scale-out 테스트 방향

로컬에서 API 인스턴스를 여러 개 띄워 검증한다.

기본 로컬 의존성:

```bash
pnpm local:up
```

```txt
API-1 localhost:8081
API-2 localhost:8082
API-3 localhost:8083
Redis localhost:6379
DynamoDB Local localhost:8000
```

현재 로컬 DynamoDB는 compose에서 in-memory 모드로 실행한다. 파일 볼륨 권한 문제 없이 바로 띄우는 것을 우선하며, 컨테이너 재시작 시 테이블과 데이터는 초기화된다. `local` Spring profile은 API 시작 시 `wowtalk-main-local`과 `wowtalk-room-events-local` 테이블을 다시 생성한다.

검증 시나리오:

```txt
Browser A -> ws://localhost:8081/ws/chat?roomId=lobby
Browser B -> ws://localhost:8082/ws/chat?roomId=lobby
Browser C -> ws://localhost:8083/ws/chat?roomId=lobby

A sends message
-> B and C receive message
```

이 시나리오가 통과하면 ECS 3 task 구조의 핵심 broadcast 문제를 로컬에서 재현하고 검증한 것이다.

## 우선순위

1. `WebSocketSessionRegistry`를 local registry로 명확히 제한한다.
2. `RealtimeEventPublisher` 추상화를 추가한다.
3. local publisher로 기존 단일 서버 동작을 유지한다.
4. DynamoDB room event stream 저장소를 구현한다.
5. Redis Pub/Sub broker 구현체를 추가한다.
6. API 3개 인스턴스 로컬 테스트를 추가한다.
