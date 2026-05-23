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

- [ ] inbound envelope DTO 추가
- [ ] outbound envelope DTO 추가
- [ ] requestId/eventId 추가
- [ ] legacy `SEND_MESSAGE`와 v1 `CHAT_SEND` 동시 지원
- [ ] error envelope 통일
- [ ] protocol parsing test 추가

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

RDS와 DynamoDB 역할을 분리할 준비를 한다.

작업:

- [ ] message repository access pattern 확정
- [ ] local DynamoDB compose 추가 여부 결정
- [ ] DynamoDB repository PoC
- [ ] RDS/DynamoDB 혼합 운영 시 transaction boundary 정리

프론트 영향:

- 낮음
- API 계약이 유지되면 영향 없음

## Milestone 8. Multi Instance Realtime

목표:

ECS에서 API 서버를 여러 대 띄워도 broadcast가 된다.

작업:

- [ ] serverInstanceId 도입
- [ ] broker 후보 결정
- [ ] Redis Pub/Sub 또는 AWS managed broker PoC
- [ ] local sessions + remote event bridge 구조 구현

프론트 영향:

- 없음 또는 낮음
- 연결 안정성은 좋아짐

## 바로 다음 작업

다음 구현은 Milestone 1부터 시작한다.

추천 첫 PR:

```txt
feat(message): add message id to chat messages
```

포함 범위:

- `MessageId` value object
- DB entity field
- JPA migration은 아직 없으므로 Hibernate update 기준 동작 확인
- REST response field
- WebSocket outbound field
- 테스트 수정

이 작업은 프론트 영향이 작고, 이후 user/protocol/game event의 기반이 된다.
