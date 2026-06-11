# AI 작업 규칙

## 1. 프로젝트 정체성
- 이 프로젝트는 `wow-talk.io` 백엔드다.
- 이 프로젝트의 핵심 목적은 채팅방 안에서 진행되는 social deduction / mission game을 위한 실시간 백엔드 구조를 만드는 것이다.
- WebSocket은 브라우저 클라이언트의 기본 realtime 진입점이다.
- `Raw TCP`는 브라우저용 주 통신 방식이 아니라 전송 계층 비교와 별도 클라이언트 실험을 위한 확장 후보로 둔다.
- 운영 구조는 ECS/Fargate API task 3대 이상, DynamoDB 중심 저장소, 서버 간 realtime event 전파를 전제로 설계한다.
- 모든 구현과 설계 판단은 단일 서버 데모보다 multi-instance realtime, DynamoDB access pattern, 모듈 경계 유지에 우선순위를 둔다.

## 2. 가장 중요한 판단 원칙
- 해결을 위한 해결을 하지 않는다.
- 에러 메시지만 보고 국소적으로 땜질하지 않는다.
- 문제를 볼 때는 항상 전체 구조, 의존성 방향, 모듈 책임, 앞으로의 확장 가능성까지 함께 본다.
- 지금 보이는 오류를 없애는 것보다, 왜 이런 구조에서 이 문제가 생겼는지를 먼저 확인한다.
- 임시 우회가 필요할 때도, 왜 임시 우회가 필요한지와 이후 원복 또는 정식 설계 방향을 분명히 남긴다.

## 3. 작업 중 의사소통 원칙
- 코드 작업 중 모르는 부분이 있으면 혼자 판단하지 말고 사용자에게 먼저 질문한다.
- 요구사항이 애매하거나 여러 해석이 가능하면 임의로 확정하지 말고 확인을 요청한다.
- 설계에 영향을 주는 선택지라면, 구현 전에 어떤 선택지가 있는지 짧게 정리하고 확인받는다.
- 특히 모듈 경계, 도메인 책임, 전송 방식 선택, 예외 정책, 패킷 구조 같은 핵심 설계는 추측으로 진행하지 않는다.

## 4. 모듈 구조
- `wowtalk-api`
  - Spring Boot 실행 모듈
  - Controller
  - Gateway
  - Configuration
- `wowtalk-core`
  - 도메인
  - Service
  - Repository
  - DTO
- `wowtalk-dynamodb`
  - DynamoDB client configuration
  - DynamoDB repository adapter
  - DynamoDB local table initializer
- `wowtalk-postgres`
  - Postgres/JPA legacy repository adapter
  - `postgres` profile에서만 활성화되는 RDS 후보 구현
- `wowtalk-redis`
  - Redis Pub/Sub realtime event publisher
  - Redis Pub/Sub realtime event subscriber
  - multi-instance WebSocket fan-out adapter
- `wowtalk-transport`
  - `ChatTransport` 인터페이스
  - `RealtimeEventPublisher` 인터페이스
  - `TransportMessage`
  - `TransportMode`
  - `SessionId`
  - `RoomId`
- `wowtalk-rawtcp`
  - `RawTcpChatTransport`
  - Raw TCP 패킷 및 프로토콜
- `wowtalk-websocket`
  - `WebSocketChatTransport`

## 5. 모듈 책임과 의존성 규칙
- `wowtalk-api`는 진입점 역할만 담당한다.
- `wowtalk-core`는 비즈니스 중심이어야 하며 전송 구현체를 직접 알면 안 된다.
- `wowtalk-dynamodb`는 DynamoDB 구현 상세를 담는 adapter 모듈이며 core가 이 모듈을 알면 안 된다.
- `wowtalk-postgres`는 Postgres/JPA 구현 상세를 담는 adapter 모듈이며 core가 이 모듈을 알면 안 된다.
- `wowtalk-redis`는 서버 간 realtime fan-out 구현 상세를 담는 adapter 모듈이며 core와 websocket이 이 모듈을 알면 안 된다.
- `wowtalk-transport`는 추상화 계층만 제공한다.
- `wowtalk-rawtcp`, `wowtalk-websocket`은 `wowtalk-transport`를 구현하는 모듈이다.
- 의존성은 반드시 안쪽 정책이 바깥 구현을 모르도록 유지한다.
- 구체 구현 선택은 `TransportRouter`, realtime publisher adapter 같은 조합 계층에서 처리한다.
- `ChatService`는 transport 구현체가 아니라 추상화에 의존해야 한다.
- `WebSocketSessionRegistry`는 전체 방 상태가 아니라 현재 API task에 붙은 local connection registry로만 취급한다.
- multi-instance broadcast는 Redis Pub/Sub, Kafka, SNS/SQS, EventBridge, DynamoDB Streams 등 별도 broker 후보를 통해 처리한다.

## 6. 패키지 구조 원칙
- 패키지는 기술 기준보다 도메인 기준으로 구성한다.
- 예시:

```text
channel/
 ├── domain
 ├── service
 ├── repository
 └── dto

message/
 ├── domain
 ├── service
 ├── repository
 └── dto
```

- `Controller`는 `wowtalk-api`에만 둔다.
- `Service`, repository interface, domain model, DTO는 `wowtalk-core`에 둔다.
- DynamoDB item mapper와 JPA entity는 각 adapter 모듈에 둔다.
- 흐름은 반드시 `Controller -> Service -> Repository`를 유지한다.
- Entity와 DTO는 분리한다.
- transport 구현 상세는 core 밖으로 새지 않게 한다.

## 7. 설계 원칙
- Controller에는 비즈니스 로직을 넣지 않는다.
- Service는 유스케이스를 표현해야 하며, 전송 구현 디테일을 품으면 안 된다.
- Repository는 영속성 책임만 가진다.
- Entity는 도메인 상태와 규칙 중심으로 설계한다.
- DTO는 외부 입출력 전용 모델로 사용한다.
- 채팅방마다 `transportMode`를 선택할 수 있어야 한다.
- transport 구현체 교체가 core 수정으로 이어지면 구조가 잘못된 것으로 본다.
- 운영 저장소는 DynamoDB access pattern을 기준으로 설계한다.
- DynamoDB는 영속 저장소이고, 서버 간 WebSocket fan-out은 별도 realtime event broker 책임으로 분리한다.
- API 서버 메모리에 global room state, global game state, 전체 presence를 두지 않는다.

## 8. 에러 처리 원칙
- 모든 예외는 공통 방식으로 처리한다.
- `@RestControllerAdvice`를 사용한다.
- `CustomException` 기반으로 관리한다.
- 예외 메시지는 한글로 작성한다.
- 서버 로그에는 requestId, errorCode, message를 남긴다.
- 예상 가능한 도메인 예외는 warn, 알 수 없는 예외는 stacktrace와 함께 error로 남긴다.
- 응답 예시는 아래 형식을 따른다.

```json
{
  "code": "CHANNEL_NOT_FOUND",
  "message": "채널을 찾을 수 없습니다."
}
```

- 에러 코드는 클라이언트와 서버가 의미를 공유할 수 있게 명확하게 정의한다.
- 단순히 예외를 숨기지 말고, 어디서 어떤 책임이 어긋났는지 드러나게 설계한다.

## 9. AOP 원칙
- AOP는 로깅과 실행 시간 측정에만 사용한다.
- 비즈니스 로직을 AOP 뒤로 숨기지 않는다.
- 핵심 로직이 AOP 없이는 이해되지 않는 구조를 만들지 않는다.
- 느린 service method 관측처럼 운영 신호를 남기는 용도로 제한한다.

## 9-1. 운영성 원칙
- 모든 HTTP 요청은 requestId를 가진다.
- 클라이언트가 `X-Request-Id`를 보내면 그대로 사용하고, 없으면 서버가 생성한다.
- 운영 헬스체크는 actuator health endpoint를 기준으로 한다.
- API container 빌드는 루트 Gradle 멀티모듈 기준으로 `:apps:api:bootJar`를 생성한다.
- Dockerfile이 새 backend 모듈을 누락하지 않도록 모듈 추가 시 함께 갱신한다.

## 10. Gradle 원칙
- Spring Boot 플러그인은 `wowtalk-api`에만 적용한다.
- 라이브러리 모듈에는 Spring Boot 플러그인을 적용하지 않는다.
- 공통 의존성 관리는 루트에서 일관되게 맞춘다.
- 모든 빌드와 실행은 `./gradlew`로 수행한다.

## 11. Lombok 원칙
- Entity에는 `@Data`를 사용하지 않는다.
- Entity에는 `@Getter`를 사용한다.
- DTO는 가능하면 `record`를 우선한다.

## 12. 구현 태도
- 급하게 돌아가게 만드는 것보다, 다음 단계에서 확장 가능한 구조를 우선한다.
- 지금 단계에서 넣는 코드가 다음 단계의 설계를 망치지 않는지 먼저 본다.
- 테스트 또는 실행이 실패하면, 실패한 현상만 수정하지 말고 구조적으로 맞는 해결인지 다시 확인한다.
- 사용자가 명시하지 않은 정책은 임의로 추가하지 않는다.
- 불확실한 상태에서 강행하지 않는다.

## 13. 사용자와 협업하는 방식
- 작업 전에 현재 이해한 요구사항과 진행 방향을 짧게 공유한다.
- 작업 중 중요한 설계 갈림길이 생기면 즉시 사용자에게 확인한다.
- 완료 보고 시에는 무엇을 바꿨는지뿐 아니라 왜 그렇게 나눴는지도 설명한다.
- 사용자가 의도한 구조와 충돌하는 부분이 보이면 바로 알린다.

## 14. 최종 체크리스트
- 이 변경이 전송 계층 비교 구조에 도움이 되는가
- 이 변경이 모듈 경계를 흐리지 않는가
- core가 구현체를 모르도록 유지되는가
- controller가 얇게 유지되는가
- 에러 처리가 일관적인가
- requestId 기반 로그와 actuator health 기준을 깨지 않았는가
- 새 backend 모듈을 추가했다면 Dockerfile과 문서도 함께 갱신했는가
- 주석은 필요한 설계 의도만 설명하고, 코드와 같은 말을 반복하지 않는가
- 임시방편이 아니라 구조적으로 설명 가능한가
- 모르는 부분을 확인 없이 혼자 결정하지 않았는가
