# wow-talk · front-end CLAUDE.md

> 이 문서는 이 저장소의 프로젝트 헌법이다. 작업/커밋 전에 본 문서가 정한 규칙을 우선한다. 본 문서와 코드가 충돌하면 본 문서를 갱신하거나 코드를 고친다 — 어느 한쪽을 침묵으로 어기지 않는다.

## 1. 한 문장 요약 & 듀얼 골

- **한 문장**: 웹소켓이 어떻게 동작하는지 채팅 옆 read-only 터미널 UI로 함께 보여주는 학습용 채팅 데모.
- **팀 골**: 처음엔 평범한 채팅앱처럼 보이다가, 채팅 입력창에 `/inspect` 같은 슬래시 커맨드를 치면 우측에 터미널이 슬라이드 인하면서 WebSocket lifecycle 로그가 찍히는 이스터에그형 데모를 완성한다.
- **개인 골(담당자)**: Vue 개발자가 React 19 / Next.js 16 App Router / WebSocket을 학습한다. 학습 누수를 막기 위해 매 커밋마다 `docs/`에 학습 노트를 남긴다.

## 2. 작업 범위 (Scope)

- 본 헌법과 모든 코드 작업은 **오직 `/Users/jeongjun/dev/wow-talk/front-end/` 안에서만** 수행한다.
- `wow-talk/back-end/`는 다른 담당자의 영역이다. 읽기 참조는 가능하지만 **수정 금지**. 백엔드 계약(엔드포인트/메시지 shape/에러 코드)도 임의 변경 금지.
- 부모 디렉토리 `wow-talk/`는 git이 아니다. front-end는 자체 git repo다. 커밋은 front-end 안에서만.
- 추후 모노레포 전환 시 본 디렉토리가 `apps/front-end` 또는 동급으로 이전된다. §14 참조.

## 3. 기술 스택 (확정)

| 영역 | 채택 | 메모 | 근거 (한 줄) |
|---|---|---|---|
| Framework | Next.js 16.2.6 | App Router only | Next 16 표준, RSC/Client 경계 학습이 이력서 직결 |
| Language | TypeScript 5 | `strict: true` | 타입 안전이 학습 노트의 디딤돌 |
| Runtime | React 19.2.4 | — | Next 16 권장 |
| 패키지 매니저 | pnpm | npm/yarn 금지 | lockfile 분기 방지 + workspace 기본기 |
| 스타일링 | vanilla-extract | CSS-in-TS, 빌드타임 추출 | DESIGN.md 토큰을 타입 그대로 직역, 런타임 0 |
| 데이터 페칭 | TanStack Query v5 | REST 캐시/재시도 | 서버 상태 표준 학습 |
| 클라이언트 상태 | Zustand | UI/터미널 로그 버퍼 등 | Vue Pinia 경험 전이 + Context 비교 학습 정답지 |
| 런타임 검증 | Zod | WS/REST 응답 parse | 백엔드 record blank 검증을 프론트도 동일 강도로 |
| 폼 검증 | (보류) | — | 필요해질 때 결정 |
| 테스트 | (보류) | vitest 후보 | 인프라가 어느 정도 굳은 뒤 |

추가 라이브러리는 본 표 갱신 + ADR(`docs/90-decisions/`) 동반 없이 도입 금지.

## 4. 백엔드 계약 (변경 금지 · 압축본)

서버: `http://localhost:8080` (Spring Boot 4 + Java 21).

### 4.1 WebSocket

- **엔드포인트**: `ws://localhost:8080/ws/chat?roomId={roomId}&sessionId={sessionId}`
- `roomId` / `sessionId` 둘 다 필수, blank 금지. 위반 시 서버가 `WEBSOCKET_CONNECTION_INVALID`로 거절.

**Client → Server (1종)**:

```json
{ "type": "SEND_MESSAGE", "payload": "..." }
```

**Server → Client (3종)**:

| type | 필드 |
|---|---|
| `CONNECTED` | `{ type, roomId, sessionId, payload(안내문), sentAt:null, code:null, message:null }` |
| `CHAT_MESSAGE` | `{ type, roomId, sessionId, payload, sentAt(ISO8601), code:null, message:null }` — 같은 룸 전체 브로드캐스트 |
| `ERROR` | `{ type:"ERROR", code, message, 나머지 null }` |

**ErrorCode 6종**: `WEBSOCKET_CONNECTION_INVALID`, `INVALID_WEBSOCKET_MESSAGE_FORMAT`, `UNSUPPORTED_MESSAGE_TYPE`, `INVALID_CHAT_MESSAGE`, `CHANNEL_NOT_FOUND`, `TRANSPORT_MODE_MISMATCH`.

### 4.2 REST (3개)

- `POST /api/v1/channels` body `{ roomId, transportMode: "WEBSOCKET" }` → 201 `{ roomId, transportMode }`
- `GET /api/v1/channels/{roomId}` → `{ roomId, transportMode }`
- `GET /api/v1/channels/{roomId}/messages?limit=50` → `[{ roomId, sessionId, payload, sentAt }, ...]` (오래된 순)

### 4.3 인증

- 인증/세션 시스템 **없음**.
- `sessionId`는 프론트가 임의로 정한다(별명/UUID). **중복 방지도 프론트 책임**.
- 인증/토큰/쿠키 도입은 본 헌법 갱신 + 백엔드 합의 선행. 임의 도입 금지.

### 4.4 참고 링크

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- 원본 계약 문서(읽기만): `../back-end/WEBSOCKET_API.md`

## 5. 폴더 구조 (청사진)

```
front-end/
├── CLAUDE.md / DESIGN.md / README.md
├── docs/                                   # §11 학습 문서 + ADR
├── public/
├── src/
│   ├── app/
│   │   ├── layout.tsx                      # 폰트 로드 + Providers 합성
│   │   ├── globals.css.ts                  # reset + body 기본
│   │   ├── page.tsx                        # 진입(룸 선택/기본 룸 redirect)
│   │   └── rooms/[roomId]/page.tsx         # 메인 채팅 페이지
│   ├── features/
│   │   ├── chat/                           # ChatPanel / MessageList / ChatComposer / useChatHistory
│   │   └── inspector/                      # InspectorPanel / InspectorLine / useInspectorBus
│   ├── components/ui/                      # 도메인 모르는 dumb 컴포넌트
│   ├── lib/
│   │   ├── api/                            # client.ts / channels.ts / messages.ts (Zod parse)
│   │   ├── ws/                             # wsClient.ts / schemas.ts / useChatSocket.ts
│   │   ├── commands/                       # parser.ts / registry.ts / handlers/
│   │   ├── env.ts                          # Zod로 검증된 환경변수 단일 export
│   │   └── id.ts                           # sessionId 생성/localStorage 저장
│   ├── styles/                             # tokens.ts / theme.css.ts / globals.css.ts
│   ├── stores/                             # Zustand (UI 상태만, 서버 상태 금지)
│   ├── hooks/                              # 공통 훅
│   ├── types/                              # api.ts / ws.ts / domain.ts (모노레포 이전 후보)
│   └── providers/                          # QueryProvider 등 Provider 합성기
├── .env.example
├── next.config.ts / package.json / pnpm-lock.yaml / pnpm-workspace.yaml
├── tsconfig.json / eslint.config.mjs
```

**원칙**:

- 이 트리는 **청사진**이다. 모든 폴더가 한 번에 생기지 않는다 — 필요해지는 시점에 만든다(YAGNI).
- 새 최상위 폴더 추가는 **본 헌법을 먼저 갱신한 뒤** 같은 커밋에 폴더 생성. `utils/`, `helpers/`, `services/` 류 자유주의 폴더 금지.
- 각 폴더 책임(한 줄):
  - `src/app/` — App Router 라우팅/layout만. 비즈니스 로직 금지.
  - `src/features/<domain>/` — 한 도메인의 UI + 도메인 훅 + 도메인 CSS.
  - `src/components/ui/` — 도메인 모르는 dumb 컴포넌트.
  - `src/lib/api/` — REST 호출 + Zod 응답 검증. 컴포넌트가 직접 fetch 금지.
  - `src/lib/ws/` — WebSocket 라이프사이클 **단일 소유**.
  - `src/lib/commands/` — 슬래시 커맨드 파서/레지스트리/핸들러.
  - `src/styles/` — 토큰 SSOT + 글로벌.
  - `src/stores/` — Zustand. 서버 상태 금지(그건 TanStack Query 담당).
  - `src/types/` — 외부 의존성 0. 모노레포 이전 후보.
  - `src/providers/` — Provider 합성기.

## 6. 명명 & Import 규칙

- **파일명**:
  - 컴포넌트: `PascalCase.tsx` (예: `ChatPanel.tsx`)
  - 훅: `useCamelCase.ts` (예: `useChatSocket.ts`)
  - 유틸/모듈: `camelCase.ts`
  - 스타일: `*.css.ts` (vanilla-extract)
  - 타입은 별도 `*.types.ts` **금지** — Zod 스키마와 같은 파일에 둔다(스키마와 타입 동기 보장).
- **Export**:
  - 컴포넌트 포함 **`default export` 사용 금지**. 모든 export는 named.
  - 예외: Next.js가 강제하는 `app/**/page.tsx`, `app/**/layout.tsx` 등 — 이때도 같은 파일에 named export 병행.
- **Import 순서**: 외부 패키지 → `@/*` alias → 같은 폴더 상대.
- **상대경로**: `../`까지만 허용. **`../../` 이상 금지** — alias로 교체.
- **`any` 금지**: `unknown`으로 받고 타입 가드/Zod로 좁힌다. 제네릭이 필요하면 제네릭.

## 7. 디자인 토큰 매핑 정책

- SSOT는 `src/styles/tokens.ts` (raw values). `theme.css.ts`에서 `createGlobalTheme(vars, tokens)`.
- `DESIGN.md`의 placeholder(`{colors.canvas}`)는 `vars.color.canvas`로 1:1 매핑. 매핑표는 `docs/50-styling/01-design-tokens-mapping.md`.
- **CSS에 hex/px 리터럴 직접 작성 금지**. 의미적 0(`0`, `100%`, `auto`)만 예외.
- `globals.css.ts`는 reset + body 기본 색/폰트만. 나머지는 컴포넌트 로컬 `*.css.ts`.
- 폰트: Copernicus/StyreneB는 비공개. 대체는 Cormorant Garamond(serif) + Inter(sans) + JetBrains Mono(mono) — Google Fonts에서 `next/font`로 로드.

## 8. 슬래시 커맨드 컨벤션

- **분기 위치**: `ChatComposer`. 입력값 첫 글자가 `/` 이고 첫 공백 전 토큰이 등록된 커맨드면 **커맨드 모드**.
- 커맨드 모드면 채팅 송신을 **하지 않고** 커맨드 핸들러를 실행한다.
- 등록되지 않은 `/xxx`는 inspector에 `[unknown command] /xxx` 출력 후 종료. **채팅 송출 금지**.
- 일반 메시지가 우연히 `/`로 시작해도(예: `/path/to/file`) 등록된 커맨드 토큰과 일치하지 않으면 일반 메시지로 송신해야 하는데, 사고 방지를 위해 v0에서는 `/`로 시작하는 모든 입력을 커맨드 모드로 다룬다(미등록이면 unknown 처리). 자연어로 `/`로 시작하고 싶다면 escape 규칙은 추후 결정.
- **v0 사전 정의 (5개)**:
  - `/inspect` — inspector 패널 토글
  - `/ws` — `/inspect` alias
  - `/clear` — inspector 로그 버퍼만 비움(채팅 영향 없음)
  - `/disconnect` — WebSocket 강제 종료, 재연결 플래그 false
  - `/help` — 사용 가능한 커맨드 목록을 inspector에 출력
- **등록 위치**: `src/lib/commands/registry.ts`. 새 커맨드 추가는 이 한 곳 + 대응 학습 노트(`docs/70-architecture/` 또는 `docs/90-decisions/`).
- **출력 채널**: 커맨드 결과는 **inspector 전용**. 채팅 메시지로 흘려보내지 않는다.

## 9. WebSocket 클라이언트 정책

- **단일 모듈 `WsClient`** (`src/lib/ws/wsClient.ts`)가 라이프사이클을 소유한다. 컴포넌트에서 `new WebSocket()` **직접 호출 금지**.
- React 측에서는 `useChatSocket` 훅으로만 접근.
- **연결**: `connect({ roomId, sessionId })` — 두 값 모두 비어있지 않을 때만 시도. blank면 inspector에 즉시 에러.
- **수신 메시지 검증**: 반드시 Zod `safeParse`. parse 실패 시 inspector에 ERROR 로그(원본 + 실패 사유), 채팅 UI에는 노출하지 않음.
- **메시지 분기**: `z.discriminatedUnion('type', [...])`로 `CONNECTED` / `CHAT_MESSAGE` / `ERROR` 분기.
- **재연결 정책 (v0)**:
  - 비정상 close(`event.wasClean === false`)에서만 재연결 시도.
  - 지수 백오프: 500ms / 1s / 2s / 4s / max 8s, **최대 5회**.
  - 시도 횟수/대기 시간/원인을 inspector에 로깅.
  - 정상 close 또는 `/disconnect` 호출 후에는 재연결하지 않는다.
- **하트비트/ping**: v0에서는 하지 않는다. 백엔드 계약에 ping이 없다 — 임의 추가 금지.
- **발신 API**: `send({ type: 'SEND_MESSAGE', payload })` helper만 공개. 다른 type은 백엔드 계약 갱신 선행.

## 10. 커밋 컨벤션

- **타입**: `feat`, `fix`, `refactor`, `chore`, `design`, `style`, `docs`, `test`, `!HOTFIX`, `!BREAKING CHANGE`
- **스코프**: 괄호로 표기. 예: `fix(ws)`, `feat(chat)`, `design(tokens)`, `style(eslint)`
- **design vs style 구분** (자주 혼동):
  - `design` — 디자이닝 변경(레이아웃/색/타이포/토큰 등 **시각 변화 있음**)
  - `style` — 코드 스타일(lint/format/import 순서 등 **시각 변화 없음**)
- **메시지 포맷** (헤더 + 빈 줄 + 본문):

  ```
  feat(ws): Zod 기반 WsClient 도입

  - safeParse 실패 시 inspector에 ERROR 로깅 (채팅 UI 비노출)
  - 비정상 close 시 지수 백오프(최대 5회) 재연결
  ```

- **Co-author 절대 추가 금지**. `Co-Authored-By:` 라인을 어떤 경우에도 붙이지 않는다. 본문은 메인 내용 + 상세 설명만.
- **작업 단위 = 커밋 단위**. 매 커밋마다 사용자에게 보고.
- **푸시**는 사용자가 명시적으로 요구할 때만.

## 11. 학습 문서(docs/) 운영 규칙

### 11.1 언제 쓰나

- 새 라이브러리/패턴 도입
- 의미 있는 리팩터
- 분기 결정 / 트레이드오프
- "한 달 뒤 자신이 다시 물을 만한" 모든 시점

오타 수정 같은 보일러플레이트성 변경은 안 쓴다.

### 11.2 포맷 (학습 노트 템플릿)

```
# NN. 제목 (yyyy-mm-dd)

## 어디서 (Where)
대상 파일/경로 + 커밋 해시(가능 시)

## 무엇을 (What)
한 줄 요약

## 왜 (Why)
도입/변경 동기

## Before / After
변경 전/후 코드 스니펫

## 장단점 (Trade-offs)
채택안 / 미채택안 각각

## 영향 (Impact)
성능·유지보수·번들 사이즈 등

## 더 읽을거리 (Refs)
공식 문서/블로그 링크
```

ADR은 별도 템플릿(`Context / Decision / Consequences`).

### 11.3 디렉토리

```
docs/
├── README.md                                 # 인덱스
├── _template/
│   ├── learning-note.md
│   └── adr.md
├── 00-project-setup/                         # 의존성 결정, 폴더 구조 결정, env 등
├── 10-nextjs/                                # App Router, RSC vs Client, fonts/metadata
├── 20-react/                                 # hooks, Context vs Zustand, useReducer vs Zustand
├── 30-typescript/                            # strict mode, Zod 패턴, narrowing
├── 40-websocket/                             # lifecycle, 재연결 전략, StrictMode 더블 마운트
├── 50-styling/                               # vanilla-extract, 디자인 토큰 매핑
├── 60-state-data/                            # TanStack Query, Zustand, 서버 상태 vs 클라이언트 상태
├── 70-architecture/                          # 슬래시 커맨드 흐름 등 단면 설계
└── 90-decisions/                             # ADR (영속적 결정 기록)
```

### 11.4 명명 규칙

- 카테고리 폴더: `NN-kebab` 두 자리 (간격 10, 끼워넣기 여유)
- 카테고리 내 파일: `NN-kebab.md` 두 자리
- ADR: `NNNN-noun-kebab.md` 네 자리 (관례 일치, 영속성 확보)

### 11.5 사용자 요청 학습 자료 (필수 항목)

- `docs/20-react/01-context-vs-zustand.md` — Context API와 Zustand 비교
- `docs/20-react/02-usereducer-vs-zustand.md` — useReducer와 Zustand 비교

## 12. 환경 변수 정책

- 모든 env는 `NEXT_PUBLIC_*` 접두사. 백엔드 인증이 없어 비밀값 없음.
- 기본값:
  - `NEXT_PUBLIC_API_BASE=http://localhost:8080`
  - `NEXT_PUBLIC_WS_BASE=ws://localhost:8080`
- `.env.local`은 gitignore(이미). `.env.example`을 커밋해 키 목록 노출.
- **코드에서 `process.env` 직접 참조 금지**. `src/lib/env.ts`에서 Zod로 parse한 단일 객체를 import.

## 13. 하지 말 것 (Hard Don'ts)

1. 백엔드 계약(엔드포인트/메시지 shape/에러 코드) 임의 변경 또는 추가
2. 인증/세션 시스템 도입 (백엔드에 없음)
3. 커밋 메시지에 `Co-Authored-By:` 라인 추가
4. 디자인 토큰 우회 — CSS에 hex/px 리터럴 직접 작성
5. `default export` 사용 (Next.js가 강제하는 `app/**/page.tsx` 등 외)
6. 컴포넌트에서 `new WebSocket()` 직접 호출
7. `npm` 또는 `yarn` 명령 사용 — pnpm 단일
8. 상대경로 import 두 단계 이상 (`../../` 금지) — alias 강제
9. `front-end/` 디렉토리 밖에서 작업/커밋
10. `wow-talk/back-end/` 수정
11. 사용자 합의 없이 라이브러리 추가 — 승인 4개 외(vanilla-extract, tanstack-query, zustand, zod) + 폰트는 예외
12. `any` 사용 — `unknown` 후 좁히기 / 제네릭
13. 슬래시 커맨드 결과를 채팅 메시지로 송출
14. `/`로 시작하는 입력을 그대로 서버로 송신

## 14. 모노레포 전환 메모

- `pnpm-workspace.yaml`이 이미 있다 — 이후 `packages:` 키만 추가하면 워크스페이스 활성화.
- 예상 구조: `apps/back-end`(현 `back-end/` wrapper), `apps/front-end`(현 디렉토리), `packages/shared`(타입/스키마 공유).
- `src/types/{api,ws,domain}.ts`는 **react/next 의존성 0**으로 유지 — `packages/shared`로 이전 쉬움.
- 백엔드 `RoomId / SessionId / TransportMessage / TransportMode`와 1:1 대응되는 타입명 유지.
- 모노레포 전환은 ADR(`docs/90-decisions/`) 작성 후 단일 커밋으로 진행한다.

## 15. 빠른 참조

- 프론트 실행: `pnpm dev`
- 린트: `pnpm lint`
- 빌드: `pnpm build`
- 백엔드 실행 (별도 터미널):

  ```
  cd /Users/jeongjun/dev/wow-talk/back-end
  docker compose up -d postgres
  ./gradlew :wowtalk-api:bootRun
  ```

- OpenAPI: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- 디자인 시스템: `./DESIGN.md`
- 학습 문서 인덱스: `./docs/README.md`

## 16. 후속 커밋 시퀀스 (가이드)

다음 순서를 권장하지만 상황에 맞게 조정 가능하다. 각 커밋마다 대응 학습 노트를 `docs/` 해당 카테고리에 동반한다.

1. `chore` — vanilla-extract / tanstack-query / zustand / zod 설치
2. `chore` — create-next-app 잔재 제거
3. `design(tokens)` — DESIGN.md → tokens.ts / theme.css.ts / globals.css.ts
4. `feat(env)` — env.ts(Zod) + .env.example
5. `feat(api)` — REST client + ensureChannel 훅
6. `feat(ws)` — WsClient + Zod schemas + useChatSocket
7. `feat(chat)` — ChatPanel + Composer + MessageList 골격
8. `feat(commands)` — 슬래시 커맨드 파서 + registry
9. `feat(inspector)` — read-only 터미널 패널 + 슬라이드 인 + WS 로그 연결
10. `style(lint)` — eslint import 순서/네이밍 룰
