# 00. 의존성 결정 (2026-05-20)

> 보일러플레이트 상태(Next 16 + React 19 + TS 5만)에서 wow-talk 프론트엔드를 굴리기 위해 필요한 런타임 의존성 5종을 한 커밋에 묶어 도입했다.

## 어디서 (Where)

- `front-end/package.json` (dependencies 7개 추가)
- `front-end/next.config.ts` (vanilla-extract 플러그인 적용)
- 커밋: `chore(deps): 핵심 의존성 설치` (헌법 §16 1번)

## 무엇을 (What)

다음 7개 패키지를 dependencies로 추가:

| 패키지 | 버전 | 역할 |
|---|---|---|
| `@vanilla-extract/css` | 1.20.1 | 타입 안전 CSS-in-TS |
| `@vanilla-extract/next-plugin` | 2.5.2 | Next.js 빌드 시 CSS 추출 통합 |
| `@tanstack/react-query` | 5.100.10 | 서버 상태(REST 캐시/재시도/리프레시) |
| `@tanstack/react-query-devtools` | 5.100.10 | 개발 시 query 상태 가시화 |
| `zustand` | 5.0.13 | 클라이언트 상태(UI 토글, sessionId 등) |
| `zod` | 4.4.3 | 런타임 검증(WS/REST 응답) |
| `nanoid` | 5.1.11 | sessionId 자동 생성용 짧은 ID |

그리고 `next.config.ts`를 `createVanillaExtractPlugin()`으로 래핑해 `.css.ts` 파일이 빌드 시 CSS로 추출되도록 했다.

## 왜 (Why)

### 1) vanilla-extract — 디자인 토큰을 타입 그대로 직역하기 위해

`DESIGN.md`는 placeholder 표기(`{colors.canvas}`)로 토큰 카탈로그를 정의한다. 이 표기는 vanilla-extract의 `createGlobalTheme(vars, tokens)` 구조와 거의 1:1 매핑이다. CSS Module은 토큰을 CSS 변수로만 노출해 TS 쪽에서 컴파일 타임 자동완성/오타 탐지가 안 된다. styled-components/emotion은 런타임 비용이 있다. vanilla-extract는 빌드 타임에 CSS로 추출되므로 런타임 비용 0, 자동완성 100%.

대안 검토:
- CSS Modules + CSS 변수: 가장 가볍지만 TS 자동완성 없음.
- Tailwind v4: 빠르지만 토큰 정의가 `@theme`에 묶이고 디자인 토큰을 "타입으로 다룬다"는 학습 목표와 멀어짐.
- styled-components/emotion: 런타임 CSS-in-JS — Next App Router에서 SSR 골치 + 번들 무거움.

### 2) TanStack Query v5 — 서버 상태 표준 학습

REST 3개(`POST /channels`, `GET /channels/:id`, `GET /channels/:id/messages`)에 대해 캐시 키 설계, `useQuery`/`useMutation`, `staleTime`/`gcTime`, 캐시 무효화 등 산업 표준 패턴을 학습한다. SWR도 후보였으나 TanStack Query가 채용 시장에서 더 자주 요구된다(이직 목표 부합).

**v5 변경점 주의**: `isLoading` → 의미가 좁아짐(첫 fetch 중에만 `true`). 일반적 "로딩 중" 표현은 `isPending` 또는 `isFetching`을 쓴다. 이 부분은 첫 사용 시점에 또 한 번 노트로 정리.

### 3) Zustand — 클라이언트 상태(가벼움 + Pinia 경험 전이)

채팅 데모에서 글로벌 상태는 `sessionId`, inspector 패널 토글, 터미널 로그 버퍼 정도. Redux는 과하고, Context API는 re-render 범위가 넓어 메시지 리스트 같은 빈도 높은 갱신에 비효율. Zustand는 selector 단위 구독으로 re-render를 좁힌다. Vue Pinia를 써본 사용자가 가장 자연스럽게 진입.

별도 학습 자료(`docs/20-react/01-context-vs-zustand.md`, `02-usereducer-vs-zustand.md`)로 비교 학습 예정.

### 4) Zod v4 — 백엔드와 동일한 검증 강도

백엔드 record가 `RoomId/SessionId`의 blank를 거부한다(`IllegalArgumentException`). 프론트도 동일 강도 검증을 두어, 잘못된 입력을 네트워크에 도달하기 전에 차단. 또한 WS 수신 메시지를 `z.discriminatedUnion('type', [...])`으로 parse하면 type별 분기에서 타입 좁히기가 자동으로 따라온다. 백엔드 계약 변경 시 즉시 parse 실패로 감지 가능.

대안 검토:
- valibot: 더 가볍지만 생태계 작음.
- 검증 없음: 가장 가볍지만 백엔드 계약 변경 시 silent 깨짐 — 학습 목적엔 부적합.

### 5) nanoid — sessionId 자동 생성

UUID v4는 36자(하이픈 포함)로 채팅 별명에 비해 길다. nanoid는 충돌 안전성이 비슷하면서 8~12자로 짧고, `customAlphabet`으로 영숫자만 추릴 수 있다. 디폴트 21자도 가능하지만 sessionId는 사용자 가시 식별자라 8자(`user-xxxxxxxx` 형태)가 적당. 또한 nanoid는 1KB 미만, uuid는 의외로 무겁다(라이브러리 의존 시).

### 6) Next.js vanilla-extract 플러그인 통합

`createVanillaExtractPlugin()`을 통해 `next.config.ts`를 래핑한다. 이로써 `.css.ts` 파일이 webpack 로더를 거쳐 빌드 타임 CSS로 추출된다. 추가 설정 없이 App Router와 동작.

## Before / After

### Before — `next.config.ts`

```ts
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
};

export default nextConfig;
```

### After — `next.config.ts`

```ts
import type { NextConfig } from "next";
import { createVanillaExtractPlugin } from "@vanilla-extract/next-plugin";

const withVanillaExtract = createVanillaExtractPlugin();

const nextConfig: NextConfig = {
  /* config options here */
};

export default withVanillaExtract(nextConfig);
```

### Before — `package.json` dependencies

```json
"dependencies": {
  "next": "16.2.6",
  "react": "19.2.4",
  "react-dom": "19.2.4"
}
```

### After — `package.json` dependencies (요약)

```json
"dependencies": {
  "@tanstack/react-query": "^5.100.10",
  "@tanstack/react-query-devtools": "^5.100.10",
  "@vanilla-extract/css": "^1.20.1",
  "@vanilla-extract/next-plugin": "^2.5.2",
  "nanoid": "^5.1.11",
  "next": "16.2.6",
  "react": "19.2.4",
  "react-dom": "19.2.4",
  "zod": "^4.4.3",
  "zustand": "^5.0.13"
}
```

## 장단점 (Trade-offs)

- **채택안**: vanilla-extract + TanStack Query + Zustand + Zod + nanoid
  - 장점: 학습 가치 큼(이직 시장 정합), 런타임 비용 최소, 타입 안전, DESIGN.md 직역.
  - 단점: 초기 설정 부담(특히 vanilla-extract), 5개 모두 처음이라 학습 곡선 분산.
- **미채택안 1**: CSS Modules + SWR + Context + 수동 타입 only
  - 장점: 가장 가벼움, Next 친화.
  - 단점: 학습 자산이 적고 실무 패턴과 거리.
- **미채택안 2**: Tailwind + TanStack Query + Zustand + Zod
  - 장점: UI 개발 속도 매우 빠름.
  - 단점: 디자인 토큰을 "타입으로 다루는" 경험 부재.

## 영향 (Impact)

- **번들 사이즈**: 5개 모두 가벼운 편. 가장 무거운 게 TanStack Query 본체(약 50KB gzip 미만). vanilla-extract는 런타임 0(빌드 타임 CSS 추출).
- **빌드 시간**: vanilla-extract 플러그인이 webpack 단계를 늘리지만 dev에서 체감 불가 수준.
- **유지보수성**: 타입 자동완성/런타임 검증으로 백엔드 계약 변경 시 즉시 컴파일/런타임 오류 — silent 깨짐 차단.
- **학습 곡선**: vanilla-extract가 가장 가파름(첫 사용 시점에 별도 노트 예정). TanStack Query v5는 v4와 API 다름 — `isLoading` 의미 변경 주의.

## 부수 결정 (이번 커밋에서 같이 처리)

### A. esbuild / @swc/core 빌드 스크립트 허용

pnpm 9+는 보안 강화로 `postinstall` 스크립트를 기본 차단한다. 새로 들어온 두 native 의존성에 대해 `pnpm-workspace.yaml`의 `allowBuilds`를 `true`로 설정.

```yaml
allowBuilds:
  '@swc/core': true   # Next.js의 JS/TS 컴파일러 (Rust 기반)
  esbuild: true       # vanilla-extract가 내부적으로 사용 (Go 기반)
  sharp: true
  unrs-resolver: true
```

- 둘 다 Vercel/Next.js 팀 공식 관리 패키지라 신뢰 가능.
- 허용 시 `postinstall`이 platform별 native 바이너리를 공식 경로로 다운로드 → 빌드/dev 속도 이점.
- 차단 시에도 `esbuild-darwin-arm64` 같은 platform-specific optional package fallback으로 동작은 하지만 셋업 경로가 줄어 일부 환경에서 비효율.

### B. webpack 모드 강제 (`next dev --webpack` / `next build --webpack`)

Next.js 16부터 **Turbopack이 기본 번들러**다. 그런데 `@vanilla-extract/next-plugin@2.5.2`는 **webpack 기반 플러그인**이라 충돌:

```
ERROR: This build is using Turbopack, with a `webpack` config and no `turbopack` config.
```

해결책으로 `package.json` scripts에 `--webpack` 플래그 명시:

```json
"scripts": {
  "dev": "next dev --webpack",
  "build": "next build --webpack",
  ...
}
```

- **트레이드오프**: Turbopack 속도 이점(특히 dev cold start)을 포기. 대신 vanilla-extract 안정 동작.
- **추후 마이그레이션 후보**: vanilla-extract가 Turbopack 정식 지원하면 `--webpack` 플래그 제거 + Turbopack 전환. ADR로 기록 예정.
- **학습 관점 손실**: Turbopack 자체 학습 기회는 줄지만, vanilla-extract로 "디자인 토큰을 타입으로 다루는" 경험이 더 우선순위.

## 더 읽을거리 (Refs)

- vanilla-extract Next.js 가이드: https://vanilla-extract.style/documentation/integrations/next/
- TanStack Query v5 마이그레이션: https://tanstack.com/query/v5/docs/framework/react/guides/migrating-to-v5
- Zod v4 changelog: https://github.com/colinhacks/zod/releases
- nanoid 공식: https://github.com/ai/nanoid
- pnpm 빌드 스크립트 정책: https://pnpm.io/settings#onlybuiltdependencies
- Next.js 16 Turbopack 기본화 안내: https://nextjs.org/docs/app/api-reference/next-config-js/turbopack
- 관련 노트: `[[design-tokens-mapping]]` (커밋 3에서 작성 예정)
