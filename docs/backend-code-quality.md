# Backend Code Quality

## 목적

백엔드 코드를 기능 구현 속도만 보고 쌓지 않고, 멀티모듈 구조와 운영 환경을 계속 유지하기 위한 기준이다.

## 모듈 경계

기본 의존성 방향은 아래를 따른다.

```txt
apps/api
  -> backend/core
  -> backend/dynamodb
  -> backend/postgres
  -> backend/redis
  -> backend/transport
  -> backend/websocket

backend/core
  -> backend/transport
```

`backend/core`는 도메인 정책과 유스케이스를 가진다.

- JPA entity를 두지 않는다.
- DynamoDB item mapper를 두지 않는다.
- Redis, WebSocket, Postgres 같은 구현체 패키지에 의존하지 않는다.
- repository는 interface만 둔다.

이 규칙은 `CoreModuleBoundaryTest`로 검증한다.

## DTO 기준

DTO는 외부 입출력 계약을 표현한다.

- REST request/response는 controller 가까이에 둔다.
- service 입력/출력은 core의 use-case DTO로 둔다.
- JPA entity, DynamoDB item, Redis message는 DTO가 아니라 adapter 내부 모델이다.
- API 응답 DTO에는 내부 구현 필드나 persistence id를 노출하지 않는다.

record를 우선 사용하되, 검증이나 변환 책임이 커지면 별도 클래스로 분리한다.

## 예외 처리 기준

도메인에서 예상 가능한 실패는 `WowTalkException` 계층으로 표현한다.

```txt
ChannelNotFoundException
TransportModeMismatchException
InvalidChatMessageException
```

Controller는 예외를 직접 try/catch하지 않는다. 공통 응답 변환은 `GlobalExceptionHandler`가 담당한다.

운영 로그 기준:

- 예상 가능한 도메인 예외: warn
- validation 실패: warn
- 알 수 없는 예외: error와 stacktrace

클라이언트 응답은 항상 아래 형태를 유지한다.

```json
{
  "code": "ERROR_CODE",
  "message": "한글 메시지"
}
```

## 로깅 기준

로그는 운영자가 검색할 수 있는 신호여야 한다.

- 모든 HTTP 요청은 `X-Request-Id`를 가진다.
- 요청 로그에는 method, uri, status, elapsedMs를 남긴다.
- service method가 500ms 이상 걸리면 slow_service 로그를 남긴다.
- payload 전체, token, 비밀번호 같은 민감 정보는 로그로 남기지 않는다.

초기에는 사람이 읽기 쉬운 text log를 사용한다. CloudWatch Logs Insights 기준 검색이 불편해지면 JSON logging을 도입한다.

## 주석 기준

주석은 “코드를 읽으면 바로 알 수 있는 내용”을 반복하지 않는다.

좋은 주석 후보:

- DynamoDB partition key / sort key를 왜 그렇게 잡았는지
- WebSocket local registry가 global registry가 아닌 이유
- Redis Pub/Sub이 영속 저장소가 아니라 fan-out 통로인 이유
- 비용, 일관성, 확장성 때문에 선택한 설계 trade-off

피해야 할 주석:

- `// user를 저장한다`
- `// null 체크`
- 메서드 이름과 같은 설명

기능 설명은 가능하면 코드 주석보다 문서에 둔다. 코드에는 해당 결정이 없으면 오해하기 쉬운 부분만 짧게 남긴다.

## 테스트 기준

테스트는 위험도에 맞춰 둔다.

- core service: 도메인 규칙과 예외 조건 검증
- adapter: 외부 기술과 매핑 규칙 검증
- api: request/response, 예외 응답, filter 같은 운영 장치 검증
- websocket: protocol parsing과 outbound envelope 검증

멀티 인스턴스 realtime은 단위 테스트만으로 부족하다. Redis broker를 사용하는 로컬 2~3개 API 인스턴스 검증 시나리오를 별도로 둔다.

## 배포 체크

backend 모듈을 추가하면 아래를 함께 확인한다.

- `settings.gradle` include 추가
- 필요한 실행 앱의 `build.gradle` 의존성 추가
- `docker/api.Dockerfile` COPY 목록 추가
- README와 백엔드 문서 구조 갱신
- `./gradlew test`
- `./gradlew :apps:api:bootJar`
- `docker build -f docker/api.Dockerfile -t wow-talk-api:local .`
