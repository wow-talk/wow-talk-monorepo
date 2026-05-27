# docs

wow-talk front-end 학습 자료 인덱스. 카테고리는 헌법(`../CLAUDE.md` §11) 정의대로.

프론트와 백엔드가 함께 봐야 하는 API/프로토콜 계약은 모노레포 루트의 `docs/`에 둔다.

- [프론트/백엔드 계약](../../../docs/frontend-backend-contract.md)
- [현재 WebSocket API](../../../docs/websocket-api.md)
- [실시간 프로토콜 v1](../../../docs/realtime-protocol-v1.md)

## 카테고리

| 폴더 | 주제 |
|---|---|
| `00-project-setup/` | 의존성 결정, 폴더 구조, env, pnpm |
| `10-nextjs/` | App Router, RSC vs Client, 폰트/메타데이터 |
| `20-react/` | hooks, Context vs Zustand, useReducer 비교 |
| `30-typescript/` | strict mode, Zod 패턴, narrowing |
| `40-websocket/` | lifecycle, 재연결 전략, StrictMode 더블 마운트 |
| `50-styling/` | vanilla-extract, 디자인 토큰 매핑 |
| `60-state-data/` | TanStack Query, Zustand, 서버 상태 vs 클라이언트 상태 |
| `70-architecture/` | 슬래시 커맨드 흐름 등 단면 설계 |
| `90-decisions/` | ADR (영속적 결정 기록) |

## 작성 규칙

- 카테고리 폴더 명: `NN-kebab` 두 자리 (간격 10, 끼워넣기 여유)
- 카테고리 내 파일: `NN-kebab.md` 두 자리
- ADR: `NNNN-noun-kebab.md` 네 자리
- 템플릿: `_template/learning-note.md`, `_template/adr.md`

## 노트 인덱스

### 00-project-setup
- [00. 의존성 결정 (2026-05-20)](00-project-setup/00-dependency-decisions.md) — vanilla-extract / TanStack Query / Zustand / Zod / nanoid 5종 선택 근거
- [02. 환경 변수 단일 진입점 (2026-05-20)](00-project-setup/02-env-and-pnpm.md) — Zod로 parse한 단일 env 객체, NEXT_PUBLIC_ 접두사 동작

### 10-nextjs
- [01. RSC vs Client Component (2026-05-20)](10-nextjs/01-rsc-vs-client.md) — use client 경계, layout RSC + Provider 분리, use(params) 패턴
- [02. next/font와 metadata (2026-05-20)](10-nextjs/02-fonts-and-metadata.md) — Cormorant Garamond + Inter + JetBrains Mono self-host, lang ko, metadata API

### 20-react
- [01. Context API vs Zustand (2026-05-20)](20-react/01-context-vs-zustand.md) — sessionId 공유 시나리오, selector 단위 re-render, Pinia 멘탈 모델 전이

### 30-typescript
- [01. Zod 런타임 검증 패턴 (2026-05-20)](30-typescript/01-zod-patterns.md) — schema+타입 추론, safeParse vs parse, discriminatedUnion, apiFetch 패턴

### 40-websocket
- [00. WebSocket 라이프사이클 (2026-05-20)](40-websocket/00-ws-lifecycle.md) — readyState 4단계, 이벤트 4종, 정상/비정상 close, 재연결 정책
- [01. WebSocket 재연결 전략 (2026-05-20)](40-websocket/01-reconnection-strategy.md) — 지수 백오프 5회 정책 실증, inspector 시각화
- [03. StrictMode 더블 마운트와 WebSocket (2026-05-20)](40-websocket/03-strictmode-double-connect.md) — effect 더블 호출, useRef+dispose 패턴

### 50-styling
- [00. vanilla-extract 인트로 (2026-05-20)](50-styling/00-vanilla-extract-intro.md) — createGlobalTheme / globalStyle / 빌드타임 추출 동작
- [01. 디자인 토큰 매핑표 (2026-05-20)](50-styling/01-design-tokens-mapping.md) — DESIGN.md placeholder → vars 1:1 매핑
- [02. CSS-in-TS 조건부 클래스 패턴 (2026-05-20)](50-styling/02-css-in-ts-tradeoffs.md) — styleVariants 패턴, 슬라이드 인, 미디어 쿼리

### 60-state-data
- [00. TanStack Query 기초 (2026-05-20)](60-state-data/00-tanstack-query-basics.md) — useQuery / useMutation / queryKey / staleTime / v5 isPending 변경점
- [02. 서버 상태 vs 클라이언트 상태 (2026-05-20)](60-state-data/02-server-state-vs-client-state.md) — 출처별 도구 분리, REST 캐시 + WS 누적 합치는 패턴

### 70-architecture
- [00. 슬래시 커맨드 흐름 (2026-05-20)](70-architecture/00-slash-command-flow.md) — 입력 → parser → registry → handler → bus → store → UI 전체 데이터 흐름

### 90-decisions
- [ADR 0004 — 슬래시 커맨드를 inspector 트리거로 채택 (2026-05-20)](90-decisions/0004-slash-command-trigger.md) — 4개 후보 비교, 채택 사유와 트레이드오프
