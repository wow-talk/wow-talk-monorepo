# Backend Implementation Backlog

## 목적

설계 문서를 실제 구현 작업으로 나누기 위한 백로그다.

우선순위 기준:

- 프론트와 병렬 작업이 가능하게 한다.
- 현재 채팅 기능을 깨지 않는다.
- 인증, DynamoDB, 게임 기능으로 확장 가능한 순서로 진행한다.

## Milestone 0. 문서와 현재 상태 정리

상태: 진행 중

- [x] 모노레포 전환
- [x] 히스토리 보존 전환
- [x] backend roadmap 작성
- [x] realtime protocol v1 초안 작성
- [x] data model 초안 작성
- [x] auth strategy 초안 작성
- [x] frontend/backend contract 초안 작성

## Milestone 1. Message Identity

목표:

메시지를 외부에서 식별 가능하게 만든다.

작업:

- [x] `MessageId` value object 추가
- [x] `ChatMessage`에 `messageId` 추가
- [x] `ChatMessageEntity`에 `message_id` 추가
- [x] `ChatMessageResponse`에 `messageId` 추가
- [x] WebSocket outbound에 `messageId` 추가
- [x] 테스트 보강

프론트 영향:

- 낮음
- message schema에 `messageId` optional 추가 후 required로 전환

## Milestone 2. User / Connection 분리

목표:

현재 `sessionId`가 발신자 역할까지 겸하는 문제를 해결한다.

작업:

- [x] `UserId` value object 추가
- [x] `ConnectionId` value object 추가
- [x] `ChatMessage`에 `senderUserId` 추가
- [x] WebSocket 연결 attribute에 userId 추가
- [x] WebSocket 연결 attribute를 connectionId까지 분리
- [ ] 기존 sessionId는 legacy compatibility로만 유지하고 신규 프론트에서 제거

프론트 영향:

- 중간
- userId와 connectionId 저장 필요

## Milestone 3. Guest Auth

목표:

로그인 없이도 안정적인 user identity를 가진다.

작업:

- [x] `User` 도메인 추가
- [ ] `AuthIdentity` 도메인 추가
- [x] guest user 생성 API 추가
- [x] displayName 검증 정책 추가
- [ ] guest -> social account linking을 고려한 구조로 repository 설계

프론트 영향:

- 중간
- 앱 시작 또는 방 입장 전에 guest 생성 API 호출

## Milestone 4. Protocol Envelope v1

목표:

채팅과 게임 이벤트를 같은 실시간 프로토콜 위에 올린다.

작업:

- [x] inbound envelope DTO 추가
- [x] outbound envelope DTO 추가
- [x] requestId 수신과 transport 전달 추가
- [x] eventId 추가
- [x] legacy `SEND_MESSAGE`와 v1 `CHAT_SEND` 동시 지원
- [x] protocolVersion=1 연결에 v1 outbound 응답
- [ ] error envelope requestId 연동
- [x] protocol parsing test 추가

프론트 영향:

- 높음
- WebSocket 송수신 schema 변경

## Milestone 5. Room / Member

목표:

방 참여자, 권한, 게임 참가 기반을 만든다.

작업:

- [ ] `Room` 도메인 정리
- [ ] 기존 `Channel`과 `Room` 관계 결정
- [x] `RoomMember` 추가
- [x] room 입장 API 추가
- [ ] room 상태 관리 추가

프론트 영향:

- 중간
- 방 입장 흐름 추가

## Milestone 6. Game Event Foundation

목표:

채팅 기반 게임 이벤트를 기록하고 broadcast할 수 있게 한다.

작업:

- [ ] `GameSession` 초안
- [ ] `GameAction` 초안
- [ ] 채팅 메시지 -> 게임 액션 해석 확장점 추가
- [ ] system message event 추가
- [ ] game event outbound 추가

프론트 영향:

- 높음
- 게임 이벤트 type별 렌더링 필요

## Milestone 7. Storage Strategy

목표:

DynamoDB 중심 저장소로 전환할 준비를 한다.

작업:

- [ ] room event stream access pattern 확정
- [ ] `wowtalk-room-events` 테이블 키 설계 확정
- [ ] `wowtalk-main` 테이블 또는 초기 분리 테이블 전략 결정
- [ ] local DynamoDB 또는 AWS dev table 검증 방식 결정
- [ ] ChatMessage / GameEvent DynamoDB repository PoC
- [ ] 기존 JPA/Postgres 구현의 유지 범위 결정

프론트 영향:

- 낮음
- API 계약이 유지되면 영향 없음

## Milestone 8. Multi Instance Realtime

목표:

ECS에서 API 서버를 3대 이상 띄워도 WebSocket broadcast가 된다.

작업:

- [ ] serverInstanceId 도입
- [ ] `WebSocketSessionRegistry`를 local connection registry로 제한
- [ ] `RealtimeEventPublisher` 추상화 추가
- [ ] broker 후보 비용/복잡도 비교
- [ ] Redis Pub/Sub, Kafka, SNS/SQS, EventBridge, DynamoDB Streams 중 PoC 후보 결정
- [ ] local sessions + remote event bridge 구조 구현
- [ ] API 3개 인스턴스 로컬 broadcast 테스트 추가

프론트 영향:

- 없음 또는 낮음
- 연결 안정성은 좋아짐

## 바로 다음 작업

현재 코드 기준으로 Milestone 1~4의 핵심 기반은 상당 부분 구현되어 있다.

추천 다음 PR 1:

```txt
docs: 실시간 스케일아웃 구조 정리
```

포함 범위:

- ECS API task 3대 이상 기준 WebSocket 한계 정리
- DynamoDB와 realtime broker 역할 분리
- Redis/Kafka/SNS/SQS/EventBridge/DynamoDB Streams 후보 비교
- 로컬 scale-out 테스트 방향 정리

추천 다음 PR 2:

```txt
feat(realtime): broadcast publisher 추상화 추가
```

포함 범위:

- `RealtimeEventPublisher` 인터페이스
- local publisher 구현
- 기존 WebSocket broadcast를 publisher 경유로 변경
- 기존 단일 서버 동작 보존 테스트

추천 다음 PR 3:

```txt
feat(storage): DynamoDB room event 저장소 초안 추가
```

포함 범위:

- DynamoDB item schema
- room event stream 저장/조회 adapter
- ChatMessage 저장소 전환 PoC
