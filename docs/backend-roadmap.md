# Backend Roadmap

## 목표

Wow Talk 백엔드는 단순 채팅 서버가 아니라, 채팅방 안에서 social deduction / mission game을 진행할 수 있는 실시간 플랫폼을 목표로 한다.

브라우저 클라이언트의 기본 realtime 진입점은 WebSocket이다. Raw TCP는 브라우저 주 통신 방식이 아니라 전송 계층 비교와 별도 클라이언트 실험을 위한 확장 후보로 둔다.

최종적으로는 다음 요구를 만족해야 한다.

- 기본 채팅이 안정적으로 동작한다.
- WebSocket 클라이언트가 동일한 의미의 메시지 계약을 사용한다.
- 서버 내부에서는 WebSocket을 기본 transport로 사용하되 Raw TCP, 이후 다른 transport로 확장할 수 있다.
- 채팅 위에 게임 이벤트를 올릴 수 있을 만큼 낮은 지연과 명확한 프로토콜을 가진다.
- AWS ECS/Fargate 환경에서 API task 3대 이상으로 확장할 수 있다.
- 운영 저장소는 DynamoDB 중심으로 설계한다.

## 현재 상태

현재 구현은 MVP 채팅의 최소 흐름을 갖고 있다.

```txt
WebSocket 연결
-> roomId/sessionId 확인
-> Channel 보장
-> WebSocketSessionRegistry 등록
-> SEND_MESSAGE 수신
-> ChatService.send()
-> 메시지 저장
-> 같은 room에 broadcast
```

모듈 구조는 장기 방향과 잘 맞는다.

```txt
apps/api              Spring Boot 실행 앱, REST API, 설정
backend/core          채널/메시지 도메인과 유스케이스
backend/dynamodb      DynamoDB adapter와 local table initializer
backend/postgres      Postgres/JPA legacy adapter
backend/redis         Redis Pub/Sub realtime broker adapter
backend/transport     전송 추상화
backend/websocket     WebSocket transport 구현
backend/rawtcp        Raw TCP transport 구현 예정
```

## 설계 원칙

### Core는 transport 구현을 모른다

`backend/core`는 채널, 유저, 메시지, 게임 이벤트 같은 정책을 다룬다. WebSocket 세션, TCP 소켓, AWS broker 같은 구현체를 직접 알면 안 된다.

### Transport는 입출력 변환에 집중한다

`backend/websocket`과 `backend/rawtcp`는 외부 연결을 내부 command/event로 바꾸고, 내부 event를 외부 프로토콜로 내보내는 역할을 맡는다.

### 프로토콜은 명시적으로 versioning한다

채팅과 게임을 같이 다루려면 메시지 타입이 빠르게 늘어난다. 처음부터 `version`, `type`, `requestId`, `roomId`, `payload`를 가진 envelope를 기준으로 잡는다.

### 여러 서버 인스턴스를 전제로 설계한다

현재 `WebSocketSessionRegistry`는 단일 JVM 안에서만 동작한다. AWS ECS에서 API 컨테이너가 2개 이상으로 늘어나면 인스턴스 간 broadcast가 필요하다.

## 추천 기술 방향

### 로그인

AWS 배포를 전제로 하면 기본 후보는 Amazon Cognito + Spring Security OAuth2 Resource Server다.

```txt
Frontend
-> Cognito 로그인
-> access token 획득
-> REST/WebSocket 요청에 token 전달
-> Backend가 JWT 검증
```

초기 개발 단계에서는 guest user를 먼저 도입하고, 이후 Cognito user로 자연스럽게 대체한다.

```txt
Phase 1: guest user
Phase 2: JWT 기반 authenticated user
Phase 3: room membership / role / permission
```

비용도 중요한 조건이므로 Cognito를 처음부터 강하게 결합하지 않는다.

인증 설계는 아래 인터페이스를 먼저 고정한다.

```txt
AuthPrincipal
  userId
  authType
  provider
  providerSubject
```

초기 구현은 guest auth로 시작하고, Cognito는 provider 중 하나로 붙인다. 이렇게 하면 Cognito 비용이나 운영 난이도가 맞지 않을 때 자체 guest/login 또는 다른 IdP로 바꾸기 쉽다.

비용 관점의 기본 판단:

- 초기 MVP/테스트: guest auth 우선
- 회원 기능 필요: Cognito Lite 또는 Essentials 검토
- 고급 보안/위험 탐지 필요: Plus는 나중에 검토
- SMS 인증/MFA는 별도 비용이 붙을 수 있으므로 초기 기본값으로 두지 않는다.

### 실시간 통신

브라우저 클라이언트는 WebSocket을 사용한다. 단, 서버 내부에서는 protocol envelope와 domain command/event를 분리한다.

```txt
Client JSON
-> WebSocket inbound message
-> RealtimeCommand
-> Domain Service
-> RealtimeEvent
-> Transport broadcast
```

Raw TCP는 브라우저 직접 지원 대상이 아니라, 서버 내부 실험 또는 별도 클라이언트용 transport로 둔다.

### DB

운영 저장소의 기본 방향은 DynamoDB다.

이 프로젝트는 RDS 기반 CRUD 서비스보다, room 단위 채팅/게임 이벤트 stream을 다루는 실시간 서비스에 가깝다. 따라서 운영에서는 유저, 방, 멤버십, 채팅 메시지, 게임 이벤트를 DynamoDB access pattern 기준으로 설계한다.

채팅 메시지와 게임 이벤트는 DynamoDB와 잘 맞는다. 핵심 access pattern이 “특정 room의 최근 이벤트 조회”이기 때문이다.

```txt
PK = ROOM#<roomId>
SK = EVT#<timestamp>#<eventId>
```

로컬 개발도 DynamoDB Local을 사용한다. 운영 저장소가 DynamoDB로 확정되었기 때문에 local과 prod의 저장소 access pattern을 최대한 맞춘다.

기존 JPA/Postgres adapter는 `postgres` profile에서만 켜지는 legacy 후보로 격리하고, 기본 local/prod 실행 경로는 DynamoDB adapter를 사용한다.

Postgres/JPA 구현은 `backend/postgres` 모듈에 둔다. `backend/core`는 repository interface와 도메인 규칙만 가진다. 이렇게 두면 운영 기본값은 DynamoDB로 유지하면서도, 나중에 RDS가 필요해질 때 adapter 모듈을 profile로 선택할 수 있다.

중요한 구분:

```txt
DynamoDB = 영속 저장소
Realtime broker = 서버 간 실시간 fan-out
```

DynamoDB는 메시지/이벤트 저장에는 적합하지만, ECS API task 3대 이상에서 WebSocket broadcast를 즉시 전파하는 통로로만 쓰기에는 부족하다. 서버 간 fan-out은 Redis Pub/Sub, Kafka, SNS/SQS, EventBridge, DynamoDB Streams 중 별도 선택이 필요하다.

### AWS 배포

컨테이너 기반 배포는 다음 형태를 기본으로 본다.

```txt
ECR
  web image
  api image

ECS Fargate
  wow-talk-web service
  wow-talk-api service

ALB
  / 또는 web domain -> web service
  /api, /ws -> api service

Data
  DynamoDB
Realtime fan-out
  Redis Pub/Sub or AWS managed broker
  Secrets Manager / SSM Parameter Store
Observability
  actuator health / metrics
  requestId 기반 structured log
```

web과 api는 별도 이미지, 별도 ECS service로 운영한다.

## 우선순위

### P0. 현재 MVP 안정화

- 현재 WebSocket 채팅 흐름 유지
- messageId 추가
- sender identity와 session identity 분리
- REST/WebSocket 응답 계약 정리
- 빈 payload, 너무 긴 payload, 잘못된 roomId 검증 강화

### P1. 유저/방/멤버십 모델 도입

- `User`
- `Room`
- `RoomMember`
- `ConnectionSession`

초기에는 guest user로 시작한다.

```txt
guest user 생성
-> room 입장
-> connection session 발급
-> WebSocket 연결
```

### P2. 실시간 프로토콜 v1 도입

- envelope 도입
- requestId/correlationId 도입
- message type 확장
- error envelope 통일
- client/server event 구분

### P3. 저장소 확장

- 운영 목표 저장소를 DynamoDB로 확정
- room event stream access pattern 문서화
- message/event repository를 DynamoDB 구현으로 추가
- local DynamoDB 검증 방식 결정

### P4. 멀티 인스턴스 broadcast

ECS API task 3대 이상을 전제로 한다.

현재 `WebSocketSessionRegistry`는 단일 JVM 안에서만 동작한다. scale-out을 고려하면 다음 중 하나가 필요하다.

- Redis Pub/Sub
- DynamoDB Streams
- SNS/SQS
- 별도 realtime gateway

초기 구현 난이도와 WebSocket fan-out 지연을 기준으로는 Redis Pub/Sub이 가장 단순하다. 비용과 AWS managed 성격을 강하게 가져가려면 DynamoDB Streams, SNS/SQS, EventBridge를 함께 검토한다.

### P5. 게임 이벤트 레이어

Among Us 같은 게임을 올리려면 채팅 메시지와 게임 이벤트를 같은 realtime protocol 위에서 다루되, domain은 분리한다.

```txt
CHAT_MESSAGE
GAME_JOIN
GAME_START
GAME_ACTION
GAME_STATE_PATCH
GAME_SYSTEM_EVENT
```

게임 상태는 강한 일관성이 필요한 영역이므로 메시지 저장소와 별도로 설계한다.

이 프로젝트의 게임은 별도 고사양 실시간 액션 게임이 아니라, 채팅방 안에서 진행되는 social deduction / mission game을 우선한다.

예상 상호작용:

- 서버가 채팅방에 미션을 부여한다.
- 유저가 특정 텍스트를 채팅창에 입력한다.
- 유저가 UI의 특정 버튼이나 영역을 클릭한다.
- 서버가 조건 달성 여부를 판정한다.
- 판정 결과가 채팅 이벤트 또는 게임 이벤트로 broadcast된다.

따라서 게임 설계는 채팅과 분리된 별도 서버보다, room 안의 event stream으로 시작한다.

```txt
Room
-> ChatMessage
-> GameMissionAssigned
-> GameActionSubmitted
-> GameStateChanged
-> SystemMessage
```

중요한 원칙:

- 채팅 메시지는 게임 입력이 될 수 있다.
- 게임 입력은 채팅 메시지와 별도로 기록될 수 있다.
- 서버가 authoritative source다.
- 클라이언트는 화면과 입력만 담당하고, 판정은 서버가 한다.
- 모든 게임 이벤트는 나중에 재생/replay 가능한 event 형태로 남기는 방향을 검토한다.

## 다음 구현 제안

현재 `messageId`, `UserId`, `ConnectionId`, guest user, room member, protocol envelope 일부는 이미 구현되어 있다.

다음 우선순위는 scale-out과 DynamoDB 전환을 위한 구조 정리다.

1. ECS 3대 이상 기준 realtime scale-out 문서 확정
2. `WebSocketSessionRegistry`를 local connection registry로 명확히 제한
3. `RealtimeEventPublisher` 추상화 추가
4. local publisher로 기존 단일 서버 동작 유지
5. DynamoDB room event stream 저장소 설계 및 adapter 초안 구현
6. broker 후보(Redis Pub/Sub, Kafka, SNS/SQS, EventBridge, DynamoDB Streams) 비용/복잡도 비교
7. 선택한 broker로 multi-instance broadcast PoC 구현

이 순서가 좋은 이유는 ECS task를 3대 이상으로 늘리는 상황에서도 WebSocket 연결과 room event 전파 책임을 분리할 수 있기 때문이다.
