# wow-talk

Wow Talk 모노레포입니다.

채팅방 안에서 social deduction / mission game을 진행할 수 있는 실시간 서비스를 목표로 합니다.

## 구조

```txt
apps/web         Next.js 프론트엔드
apps/api         Spring Boot API 실행 앱
backend/core     백엔드 도메인, 서비스, repository interface
backend/dynamodb DynamoDB adapter
backend/postgres  Postgres/JPA legacy adapter
backend/redis    Redis Pub/Sub realtime broker adapter
backend/transport
backend/websocket
backend/rawtcp
infra/terraform  Terraform 인프라
docker           운영 Dockerfile
```

## 로컬 실행

```bash
pnpm install
pnpm local:up
pnpm dev:api
pnpm dev:web
```

기본 접속 주소:

```txt
web      http://localhost:3000
api      http://localhost:8080
swagger  http://localhost:8080/swagger-ui.html
health   http://localhost:8080/actuator/health
```

프론트엔드는 아래 값을 기준으로 백엔드에 연결합니다.

```txt
NEXT_PUBLIC_API_BASE=http://localhost:8080
NEXT_PUBLIC_WS_BASE=ws://localhost:8080
```

백엔드는 기본적으로 `local` Spring profile로 실행됩니다. 로컬 저장소도 운영 방향과 맞추기 위해 DynamoDB Local을 사용합니다.

DynamoDB Local은 간단한 로컬 세팅을 위해 in-memory 모드로 실행합니다. 컨테이너를 재시작하면 테이블과 데이터가 초기화되며, API가 시작될 때 local profile이 아래 테이블을 다시 생성합니다.

```txt
wowtalk-main-local
wowtalk-room-events-local
```

기본 realtime broker는 `local`입니다. API 서버 1대 로컬 개발에서는 local broker가 바로 WebSocket에 broadcast합니다.

여러 API 인스턴스 broadcast를 검증할 때는 Redis broker를 사용합니다.

```bash
WOWTALK_REALTIME_BROKER=redis ./gradlew :apps:api:bootRun
```

로컬 의존성 종료:

```bash
pnpm local:down
```

## 빌드

```bash
pnpm build:web
pnpm build:api
```

백엔드 테스트:

```bash
./gradlew test
```

## 문서

```txt
docs/          제품, 백엔드, API 계약, 인프라 방향
apps/web/docs  프론트엔드 구현 기록
```

프론트/백엔드 공용 계약은 아래 문서를 기준으로 합니다.

- [docs/team-workflow.md](docs/team-workflow.md)
- [docs/frontend-backend-contract.md](docs/frontend-backend-contract.md)
- [docs/websocket-api.md](docs/websocket-api.md)
- [docs/realtime-protocol-v1.md](docs/realtime-protocol-v1.md)
- [docs/realtime-scaleout.md](docs/realtime-scaleout.md)

## AWS 배포 방향

`web`과 `api`는 각각 별도 컨테이너 이미지로 빌드하고, ECR에 push한 뒤 ECS/Fargate 서비스로 배포하는 방향입니다.

백엔드는 처음부터 API task 3대 이상을 전제로 설계합니다.

```txt
ALB
  /      -> web service
  /api   -> api service
  /ws    -> api service

api service
  desired count >= 3
```

운영 저장소 방향:

```txt
DynamoDB
  user / room / member 상태
  room event stream
  chat messages
  game events
```

실시간 scale-out 방향:

```txt
WebSocket 연결은 각 API task의 local 상태로만 관리한다.
room event는 realtime broker를 통해 모든 API task로 전파한다.
각 API task는 자신에게 연결된 local socket에만 broadcast한다.
```

자세한 scale-out 설계는 [docs/realtime-scaleout.md](docs/realtime-scaleout.md)를 기준으로 합니다.

운영 로그와 헬스체크 기준은 [docs/backend-operability.md](docs/backend-operability.md)를 기준으로 합니다.
