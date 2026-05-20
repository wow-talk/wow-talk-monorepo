# 02. 환경 변수 단일 진입점 (2026-05-20)

> Next.js의 `process.env`를 모듈 곳곳에서 직접 참조하지 않고, Zod로 parse한 단일 `env` 객체만 import하도록 강제한다. 잘못된 환경 변수는 빌드/dev 시작 단계에서 즉시 throw.

## 어디서 (Where)

- `src/lib/env.ts` — 스키마 정의 + parse + export
- `.env.example` — 키 목록과 기본값 노출 (커밋 대상)
- `.env.local` — 환경별 덮어쓰기 (gitignore, 커밋 금지)

## 무엇을 (What)

```ts
const envSchema = z.object({
  NEXT_PUBLIC_API_BASE: z.string().url().default("http://localhost:8080"),
  NEXT_PUBLIC_WS_BASE: z.string().url().default("ws://localhost:8080"),
});

const parsed = envSchema.safeParse({
  NEXT_PUBLIC_API_BASE: process.env.NEXT_PUBLIC_API_BASE,
  NEXT_PUBLIC_WS_BASE: process.env.NEXT_PUBLIC_WS_BASE,
});

if (!parsed.success) throw new Error(`[env] ...`);
export const env = parsed.data;
```

사용 측:

```ts
import { env } from "@/lib/env";
fetch(`${env.NEXT_PUBLIC_API_BASE}/api/v1/channels`);
```

## 왜 (Why)

### 1) `process.env` 산포가 위험한 이유

- 타이핑이 `string | undefined` — 모든 사용처에서 `??` fallback을 매번 작성하게 됨. 어떤 곳은 fallback이 있고 어떤 곳은 없으면 환경별 동작이 미묘하게 어긋남.
- 키 오타 시 컴파일러가 못 잡음 (`process.env.NEXT_PUBIC_API_BASE` → undefined).
- 비밀 키와 공개 키 구분이 코드 흐름에서 안 보임.

### 2) Zod로 parse하는 이유

- 모듈 로드 시점에 검증 → 환경 변수 누락/오타가 dev 시작/빌드 단계에서 즉시 잡힘. 런타임 한참 후 fetch 실패로 발견되는 사고 차단.
- 타입 추론으로 `env.NEXT_PUBLIC_API_BASE`는 `string`(undefined 없음).
- url() 같은 validator로 형식까지 검증.

### 3) `NEXT_PUBLIC_*` 접두사 동작

Next.js는 `NEXT_PUBLIC_`으로 시작하는 환경 변수만 **클라이언트 번들에 인라인**한다. 접두사 없는 변수는 서버 전용. 본 프로젝트는 백엔드 인증이 없어 비밀 키 자체가 없고, 모든 값을 클라이언트에서 써야 하므로 전부 `NEXT_PUBLIC_*`. 만약 추후 비밀 키가 생기면 별도 server-only env 객체로 분리.

### 4) `.default()`로 로컬 기본값 제공

`.env.local`이 없어도 로컬 개발 환경(localhost:8080 백엔드)이 그대로 동작. 신규 합류자가 셋업 부담 없이 `pnpm dev`로 시작 가능.

## Before / After

### Before — 분산 fetch

```ts
// 각 fetch마다 반복
const base = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080";
fetch(`${base}/api/v1/channels`);
```

문제:
- 매번 fallback 작성 (실수로 누락 가능)
- 타입이 `string | undefined`
- 오타 발견 불가

### After — 단일 import

```ts
import { env } from "@/lib/env";
fetch(`${env.NEXT_PUBLIC_API_BASE}/api/v1/channels`);
```

장점:
- 타입은 `string` 단일
- 키 오타는 컴파일 에러
- parse 실패는 모듈 로드 시점 throw

## 장단점 (Trade-offs)

- **채택안: Zod parse + single export**
  - 장점: 타입 안전, 즉시 실패, 키 카탈로그가 한 곳.
  - 단점: 새 env 추가 시 schema도 같이 수정해야 함(좋은 부담).
- **미채택안: `process.env` 직접 참조**
  - 장점: 셋업 0.
  - 단점: 위 문제 그대로.
- **미채택안: `@t3-oss/env-nextjs`** (서드파티)
  - 장점: 동일 패턴을 라이브러리로 제공.
  - 단점: 추가 의존성. Zod로 직접 짜는 게 학습 가치 큼.

## 영향 (Impact)

- 빌드 타임 부담 0 (parse 한 번).
- 런타임 부담 0 (이미 string 객체).
- 신규 합류자가 .env.local 없이도 dev 가능.

## 더 읽을거리 (Refs)

- Next.js 환경 변수: https://nextjs.org/docs/app/api-reference/file-conventions/env
- Zod 스키마: https://zod.dev
- 관련 노트: [[01-zod-patterns]] (커밋 5에서 작성)
