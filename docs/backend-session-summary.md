# Session Summary

## 1. 현재 프로젝트 상태
- 프로젝트는 Gradle 멀티 모듈 구조로 전환 완료
- `WebSocket` 우선 전략으로 실시간 채팅 기본 흐름 구현 완료
- `ChatTransport` 추상화를 유지하고 있어 이후 `Raw TCP` 구현 추가 가능
- 로컬 개발 기준으로 `PostgreSQL` 영속화 구조까지 진입
- Swagger, WebSocket 테스트 페이지, WebSocket 계약 문서 준비 완료

## 2. 현재 모듈 구조
- `wowtalk-api`
  - Spring Boot 실행 모듈
  - REST Controller
  - Swagger/OpenAPI 설정
  - 공통 예외 처리
- `wowtalk-core`
  - 채널/메시지 도메인
  - Service
  - Repository 인터페이스
  - JPA 엔티티 및 JPA 어댑터
  - 공통 도메인 예외
- `wowtalk-transport`
  - `ChatTransport`
  - `TransportRouter`
  - `TransportMessage`
  - `TransportMode`
  - `RoomId`
  - `SessionId`
- `wowtalk-websocket`
  - WebSocket transport 구현
  - WebSocket handler
  - 세션 관리
  - 입출력 JSON DTO
- `wowtalk-rawtcp`
  - 아직 본격 구현 전
  - 모듈 골격만 생성됨

## 3. 현재 구현 완료 범위

### 멀티 모듈
- 루트 `build.gradle`, `settings.gradle` 정리 완료
- Boot plugin은 `wowtalk-api`에만 적용
- 라이브러리 모듈은 공통 BOM 기반으로 동작

### Transport 추상화
- `ChatTransport`
- `TransportRouter`
- `TransportMessage`
- `TransportMode`
- `RoomId`
- `SessionId`

### WebSocket 채팅
- 연결 주소:
```text
ws://localhost:8080/ws/chat?roomId={roomId}&sessionId={sessionId}
```
- 현재 메시지 계약은 JSON 기반
- 지원 타입:
  - `SEND_MESSAGE`
  - `CONNECTED`
  - `CHAT_MESSAGE`
  - `ERROR`

### REST API
- 채널 생성 또는 보장
- 채널 조회
- 최근 메시지 조회

### Swagger / 문서
- Swagger UI 제공
- WebSocket 계약 문서 제공
- 브라우저 WebSocket 테스트 페이지 제공

### 예외 처리
- 공통 `WowTalkException`
- 공통 HTTP 예외 응답
- WebSocket 연결 오류 및 메시지 형식 오류 처리

### 영속화
- `ChannelEntity`
- `ChatMessageEntity`
- JPA Repository 및 Adapter 추가
- `local` 프로필은 PostgreSQL 사용
- `test` 프로필은 메모리 저장소 사용

## 4. 현재 주요 파일

### 루트
- [AI_RULES.md](/Users/najeong-gyun/dev/team/wow-talk/AI_RULES.md:1)
- [WEBSOCKET_API.md](/Users/najeong-gyun/dev/team/wow-talk/WEBSOCKET_API.md:1)
- [compose.yaml](/Users/najeong-gyun/dev/team/wow-talk/compose.yaml:1)
- [build.gradle](/Users/najeong-gyun/dev/team/wow-talk/build.gradle:1)
- [settings.gradle](/Users/najeong-gyun/dev/team/wow-talk/settings.gradle:1)

### API
- [WowTalkApplication.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-api/src/main/java/io/wowtalk/WowTalkApplication.java:1)
- [ChannelController.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-api/src/main/java/io/wowtalk/chat/controller/ChannelController.java:1)
- [ChatMessageController.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-api/src/main/java/io/wowtalk/chat/controller/ChatMessageController.java:1)
- [OpenApiConfig.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-api/src/main/java/io/wowtalk/common/config/OpenApiConfig.java:1)
- [GlobalExceptionHandler.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-api/src/main/java/io/wowtalk/common/error/GlobalExceptionHandler.java:1)

### Core
- [DefaultChannelService.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-core/src/main/java/io/wowtalk/channel/service/DefaultChannelService.java:1)
- [DefaultChatService.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-core/src/main/java/io/wowtalk/message/service/DefaultChatService.java:1)
- [ChannelEntity.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-core/src/main/java/io/wowtalk/channel/domain/ChannelEntity.java:1)
- [ChatMessageEntity.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-core/src/main/java/io/wowtalk/message/domain/ChatMessageEntity.java:1)
- [ChannelRepositoryJpaAdapter.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-core/src/main/java/io/wowtalk/channel/repository/ChannelRepositoryJpaAdapter.java:1)
- [ChatMessageRepositoryJpaAdapter.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-core/src/main/java/io/wowtalk/message/repository/ChatMessageRepositoryJpaAdapter.java:1)

### WebSocket
- [WebSocketChatHandler.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-websocket/src/main/java/io/wowtalk/websocket/transport/WebSocketChatHandler.java:1)
- [WebSocketChatTransport.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-websocket/src/main/java/io/wowtalk/websocket/transport/WebSocketChatTransport.java:1)
- [WebSocketInboundMessage.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-websocket/src/main/java/io/wowtalk/websocket/transport/WebSocketInboundMessage.java:1)
- [WebSocketOutboundMessage.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-websocket/src/main/java/io/wowtalk/websocket/transport/WebSocketOutboundMessage.java:1)
- [WebSocketTransportConfig.java](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-websocket/src/main/java/io/wowtalk/websocket/transport/WebSocketTransportConfig.java:1)

### 설정
- [application.yaml](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-api/src/main/resources/application.yaml:1)
- [application-local.yaml](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-api/src/main/resources/application-local.yaml:1)
- [application-prod.yaml](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-api/src/main/resources/application-prod.yaml:1)
- [application-test.yaml](/Users/najeong-gyun/dev/team/wow-talk/wowtalk-api/src/main/resources/application-test.yaml:1)

## 5. 현재 실행/확인 방법

### 테스트
```bash
./gradlew test
```

### 로컬 PostgreSQL 실행
OrbStack이 켜져 있어야 함

```bash
docker compose up -d postgres
```

### 애플리케이션 실행
```bash
./gradlew :wowtalk-api:bootRun
```

### 주요 접속 경로
- Swagger:
```text
http://localhost:8080/swagger-ui.html
```
- OpenAPI JSON:
```text
http://localhost:8080/v3/api-docs
```
- WebSocket 테스트 페이지:
```text
http://localhost:8080/ws-test.html
```

## 6. 현재 API 요약

### 채널 생성 또는 보장
```http
POST /api/v1/channels
```

예시 요청:
```json
{
  "roomId": "room-1",
  "transportMode": "WEBSOCKET"
}
```

### 채널 조회
```http
GET /api/v1/channels/{roomId}
```

### 최근 메시지 조회
```http
GET /api/v1/channels/{roomId}/messages?limit=50
```

## 7. 현재 WebSocket 계약 요약

### 연결
```text
ws://localhost:8080/ws/chat?roomId=room-1&sessionId=user-1
```

### 클라이언트 -> 서버
```json
{
  "type": "SEND_MESSAGE",
  "payload": "안녕하세요"
}
```

### 서버 -> 클라이언트
연결 성공:
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

채팅 메시지:
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

에러:
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

## 8. 현재 주의사항
- `local` 프로필은 이제 PostgreSQL이 떠 있어야 정상 기동
- OrbStack Docker daemon이 꺼져 있으면 `docker compose` 실패
- `test` 프로필은 메모리 저장소를 사용하므로 DB 없이 테스트 가능
- 현재 `Raw TCP`는 아직 구현 시작 전
- Redis/Kafka는 아직 미도입
- 현재 목적은 WebSocket을 먼저 완성하고, 같은 계약으로 Raw TCP를 추가하는 것

## 9. 다음 세션에서 우선할 작업 추천
1. 채널 생성 -> WebSocket 메시지 전송 -> DB 저장 -> 최근 메시지 조회까지 E2E 점검
2. 프론트 연동 관점에서 `sessionId`, `nickname`, `messageId` 정책 정리
3. 메시지 엔티티를 향후 수정/삭제/읽음 처리까지 확장 가능하게 다듬기
4. 그 다음 `Raw TCP` 패킷/프로토콜 설계 시작

## 10. 현재 방향 한 줄 정리
- WebSocket을 먼저 완성하고
- PostgreSQL에 저장 가능한 구조를 만들고
- 프론트가 붙을 수 있는 계약을 고정한 뒤
- 같은 계약으로 Raw TCP를 추가해 비교한다
