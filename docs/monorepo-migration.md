# Wow Talk Monorepo Migration

## 목적

기존에 분리되어 있던 프론트엔드 레포와 백엔드 레포를 하나의 제품 단위 저장소로 합쳤다.

- 프론트엔드: `wow-talk-frontend`
- 백엔드: `wow-talk-backend`
- 신규 모노레포: `wow-talk-monorepo`

프론트엔드는 다른 사람이 관리하는 코드라는 조건이 있었기 때문에, 앱 소스와 설정은 수정하지 않고 그대로 옮겼다.

## 최종 구조

```txt
wow-talk-monorepo/
  apps/
    web/                    # Next.js 프론트엔드
    api/                    # Spring Boot 실행 애플리케이션

  backend/
    core/                   # 기존 wowtalk-core
    transport/              # 기존 wowtalk-transport
    websocket/              # 기존 wowtalk-websocket
    rawtcp/                 # 기존 wowtalk-rawtcp

  infra/
    terraform/              # AWS/Terraform 인프라 자리

  docker/
    web.Dockerfile
    api.Dockerfile

  compose.yaml              # 로컬 DynamoDB
  package.json              # 루트 실행 스크립트
  pnpm-workspace.yaml       # 프론트 workspace
  settings.gradle           # 백엔드 Gradle 멀티모듈
  build.gradle              # 백엔드 공통 Gradle 설정
```

## 왜 이렇게 바꿨나

### `apps/`

실제로 실행되는 애플리케이션만 둔다.

- `apps/web`: Next.js 서버
- `apps/api`: Spring Boot API 서버

이렇게 두면 나중에 `apps/admin`, `apps/worker`, `apps/batch` 같은 실행 단위가 추가되어도 기준이 명확하다.

### `backend/`

백엔드 내부 모듈은 실행 앱이 아니라 재사용되는 서버 모듈이므로 `backend/` 아래로 분리했다.

기존 백엔드 레포는 이미 Gradle 멀티모듈 구조였기 때문에, 그 구조를 해체하지 않고 모노레포 루트의 Gradle 멀티모듈로 승격했다.

변경 전:

```txt
wow-talk-backend/
  wowtalk-api/
  wowtalk-core/
  wowtalk-transport/
  wowtalk-websocket/
  wowtalk-rawtcp/
```

변경 후:

```txt
wow-talk-monorepo/
  apps/api/
  backend/core/
  backend/transport/
  backend/websocket/
  backend/rawtcp/
```

### `infra/terraform`

AWS 컨테이너 기반 배포를 고려해서 Terraform 영역을 미리 만들었다.

예상 방향:

- ECR: web/api 이미지 저장
- ECS Fargate: 컨테이너 실행
- ALB: web/api 라우팅
- DynamoDB: 운영 DB
- Secrets Manager 또는 SSM Parameter Store: DB 비밀번호 등 secret 주입

### `docker/`

프론트와 백엔드는 별도 컨테이너 이미지로 배포하는 쪽이 유지보수에 좋다.

- `docker/web.Dockerfile`
- `docker/api.Dockerfile`

프론트와 백엔드를 한 컨테이너에 묶지 않은 이유:

- 프론트만 바뀌었을 때 백엔드 재배포가 필요 없음
- 백엔드만 독립적으로 스케일아웃 가능
- 로그, 헬스체크, 롤백 단위가 명확함
- ECS 서비스와 Terraform 모듈을 분리하기 쉬움

## Gradle 변경

루트에 `settings.gradle`을 만들고, 백엔드 모듈을 새 경로 기준으로 다시 등록했다.

```gradle
rootProject.name = 'wow-talk'

include 'apps:api'
include 'backend:core'
include 'backend:transport'
include 'backend:rawtcp'
include 'backend:websocket'
```

백엔드 의존성도 새 프로젝트 경로로 바꿨다.

예:

```gradle
implementation project(':backend:core')
implementation project(':backend:transport')
implementation project(':backend:rawtcp')
implementation project(':backend:websocket')
```

수정된 백엔드 Gradle 파일:

- `apps/api/build.gradle`
- `backend/core/build.gradle`
- `backend/websocket/build.gradle`
- `backend/rawtcp/build.gradle`

## pnpm 변경

루트에 `package.json`, `pnpm-workspace.yaml`, `pnpm-lock.yaml`을 만들었다.

루트 명령:

```bash
pnpm dev:web
pnpm dev:api
pnpm build:web
pnpm build:api
pnpm db:up
pnpm db:down
```

처음에는 `pnpm --dir apps/web` 방식으로 실행하려 했지만, 프론트 내부의 `pnpm-workspace.yaml` 때문에 pnpm이 실패했다.

그래서 루트 workspace 기준으로 실행되도록 바꿨다.

```json
{
  "dev:web": "pnpm --filter ./apps/web dev",
  "build:web": "pnpm --filter ./apps/web build",
  "lint:web": "pnpm --filter ./apps/web lint"
}
```

## 프론트엔드 수정 여부

프론트엔드 앱 코드는 수정하지 않았다.

수정하지 않은 영역:

- `apps/web/src/**`
- `apps/web/package.json`
- `apps/web/next.config.ts`
- `apps/web/tsconfig.json`
- `apps/web/eslint.config.mjs`
- `apps/web/.env.example`

즉, 프론트 앱 코드와 Next/TypeScript/ESLint 설정은 원본 레포에서 복사된 상태 그대로다.

프론트와 관련해서 새로 추가되거나 조정된 것은 모노레포 루트 설정과 Dockerfile 쪽이다.

- `package.json`
- `pnpm-workspace.yaml`
- `pnpm-lock.yaml`
- `docker/web.Dockerfile`
- `apps/web/pnpm-workspace.yaml` 제거
- `apps/web/pnpm-lock.yaml` 제거

## 프론트 pnpm/Next 경고 해결

초기 전환 직후에는 프론트 빌드가 성공하더라도 Next.js가 다음 경고를 띄웠다.

```txt
Next.js inferred your workspace root, but it may not be correct.
Detected additional lockfiles:
  apps/web/pnpm-workspace.yaml
```

원인은 프론트가 단독 레포였을 때의 `apps/web/pnpm-workspace.yaml`, `apps/web/pnpm-lock.yaml`이 모노레포 내부에 그대로 남아 있었기 때문이다.

모노레포에서는 루트 `pnpm-workspace.yaml`과 루트 `pnpm-lock.yaml`이 단일 기준이 되어야 하므로, 중복 파일을 제거했다.

이 변경은 프론트 런타임 코드 변경이 아니라 패키지 관리 파일 정리다.

## 검증 결과

백엔드:

```bash
./gradlew projects
./gradlew :apps:api:build
```

결과:

```txt
BUILD SUCCESSFUL
```

프론트:

```bash
npx pnpm@10.0.0 install
npx pnpm@10.0.0 build:web
```

결과:

```txt
Next.js production build successful
```

## 로컬 실행 순서

```bash
cd /Users/dotseven/Developer/Practice/codex/wow-talk-monorepo

pnpm install
pnpm db:up
pnpm dev:api
pnpm dev:web
```

프론트 기본 환경변수:

```txt
NEXT_PUBLIC_API_BASE=http://localhost:8080
NEXT_PUBLIC_WS_BASE=ws://localhost:8080
```

백엔드는 기본 `local` profile로 실행되며, `compose.yaml`의 DynamoDB Local을 사용한다.

로컬 프론트 연동을 위해 백엔드는 기본 CORS origin으로 `http://localhost:3000`을 허용한다.
운영에서는 `WOWTALK_CORS_ALLOWED_ORIGINS` 환경변수로 실제 프론트 도메인을 주입한다.

## 새 레포로 올릴 때

```bash
cd /Users/dotseven/Developer/Practice/codex/wow-talk-monorepo
git init
git add .
git commit -m "chore: initialize monorepo"
git remote add origin <새 GitHub 레포 URL>
git push -u origin main
```
