# Backend Operability

## 목적

백엔드가 ECS/Fargate에서 여러 task로 실행될 때 운영자가 상태를 확인하고, 장애를 추적하고, 컨테이너를 안전하게 교체할 수 있게 만드는 기준이다.

## 요청 추적

모든 HTTP 요청은 `X-Request-Id`를 가진다.

- 클라이언트나 ALB가 `X-Request-Id`를 보내면 서버는 그 값을 그대로 사용한다.
- 값이 없으면 API 서버가 UUID를 생성한다.
- 응답에도 같은 `X-Request-Id`를 내려준다.
- 애플리케이션 로그 MDC에는 `requestId`로 저장한다.

기본 요청 로그 형식:

```txt
http_request method=<METHOD> uri=<URI> status=<STATUS> elapsedMs=<MILLIS>
```

이 값은 CloudWatch Logs에서 특정 요청의 흐름을 추적하는 기준으로 사용한다.

## 예외 로그

예외 응답은 공통 JSON 형식을 유지한다.

```json
{
  "code": "CHANNEL_NOT_FOUND",
  "message": "채널을 찾을 수 없습니다."
}
```

로그 레벨 기준:

- 도메인에서 예상 가능한 `WowTalkException`: `warn`
- validation 실패: `warn`
- 예상하지 못한 서버 예외: `error`와 stacktrace

클라이언트에는 내부 stacktrace를 노출하지 않는다. 운영자는 로그의 `requestId`로 상세 원인을 추적한다.

## 성능 관측

Service 계층에는 느린 실행을 감지하는 AOP가 적용된다.

```txt
slow_service method=<CLASS.METHOD> elapsedMs=<MILLIS>
```

현재 기준은 500ms 이상이다. 이 값은 초기 운영 기준이며, 트래픽 패턴이 보이면 조정한다.

## 헬스체크

API는 Spring Boot actuator를 사용한다.

```txt
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

ECS target group health check는 `/actuator/health`를 기준으로 둔다.

Kubernetes가 아니라 ECS/Fargate 기준이라도 actuator의 liveness/readiness probe 설정은 유지한다. 이후 배포 환경이 바뀌어도 같은 애플리케이션 상태 모델을 사용할 수 있기 때문이다.

## 컨테이너 빌드

API 이미지는 루트 Gradle 멀티모듈 프로젝트에서 생성한다.

```bash
docker build -f docker/api.Dockerfile -t wow-talk-api .
```

Dockerfile은 `:apps:api:bootJar`를 실행한다. 따라서 API가 의존하는 backend 모듈을 추가하면 Dockerfile의 `COPY backend/<module>/...` 목록도 함께 갱신해야 한다.

현재 API 이미지에 포함되는 주요 모듈:

```txt
apps/api
backend/core
backend/dynamodb
backend/postgres
backend/redis
backend/transport
backend/rawtcp
backend/websocket
```

## 운영 기본값

저장소 기본값은 DynamoDB다. Postgres/JPA는 `postgres` profile에서만 활성화되는 legacy adapter로 유지한다.

실시간 fan-out 기본 방향은 다음과 같다.

```txt
WebSocket connection: 각 API task local memory
Persistent event/message: DynamoDB
Cross-task broadcast: Redis Pub/Sub 또는 AWS managed broker 후보
```

즉, API task가 3대 이상이어도 특정 task가 전체 연결 상태를 소유하지 않는다. 각 task는 자신에게 붙은 socket만 관리하고, 서버 간 전파는 broker가 담당한다.
