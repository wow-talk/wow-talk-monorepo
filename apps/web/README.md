# apps/web

> wow-talk 모노레포의 프론트엔드 워크스페이스

**웹소켓이 어떻게 동작하는지 채팅 옆 read-only 터미널 UI로 함께 보여주는 학습용 채팅 데모**.  
처음엔 평범한 채팅앱처럼 보이다가, 입력창에 슬래시 커맨드를 치면 우측에서 inspector 터미널이 슬라이드 인하면서 WebSocket lifecycle 로그가 흐른다.

## 기능

- 룸 단위 실시간 채팅 (`/rooms/[roomId]`, 기본 `/rooms/lobby`로 redirect)
- 첫 진입 시 `user-{8자리}` sessionId 자동 생성 + localStorage 영속, 헤더 칩 클릭으로 변경 모달 → WS 자동 재연결
- REST 히스토리 + WS 실시간 메시지를 시간순 단일 배열로 합쳐 렌더
- 비정상 close 시 지수 백오프(500ms / 1s / 2s / 4s / 8s, 최대 5회) 자동 재연결
- 슬래시 커맨드로 inspector 토글 / WS 강제 종료 / 로그 비움 / 도움말

## 기술 스택

| 영역            | 채택                                       |
| --------------- | ------------------------------------------ |
| Framework       | Next.js 16.2.6 (App Router, webpack 모드)  |
| Language        | TypeScript 5 strict                        |
| Runtime         | React 19.2.4                               |
| 패키지 매니저   | pnpm (모노레포 워크스페이스)               |
| 스타일링        | vanilla-extract (CSS-in-TS, 빌드타임 추출) |
| 서버 상태       | TanStack Query v5                          |
| 클라이언트 상태 | Zustand                                    |
| 런타임 검증     | Zod (REST/WS 응답 모두 parse)              |
| sessionId 생성  | nanoid                                     |

Turbopack 대신 webpack 모드를 사용한다 (`next dev --webpack` / `next build --webpack`). vanilla-extract가 webpack 플러그인 기반이라 Next 16 기본 Turbopack과 충돌. 자세한 사유는 [docs/00-project-setup/00-dependency-decisions.md](docs/00-project-setup/00-dependency-decisions.md) 부수 결정 B.

## 실행

### 모노레포 root

```bash
pnpm install            # 모노레포 전체 의존성
pnpm db:up              # Postgres (백엔드 의존)
pnpm dev:api            # 백엔드 (별도 터미널)
pnpm dev:web            # 프론트 dev 서버 → http://localhost:3000
```

### apps/web

```bash
cd apps/web
pnpm dev                # next dev --webpack
pnpm build              # next build --webpack
pnpm lint               # eslint
```

브라우저에서 `http://localhost:3000` 접속 시 `/rooms/lobby`로 redirect. 두 탭을 열어 메시지 송수신을 확인한다.

## 환경 변수

`apps/web/src/lib/env.ts`가 Zod로 parse한 단일 `env` 객체만 export한다. 코드에서 `process.env` 직접 참조 금지.

```bash
NEXT_PUBLIC_API_BASE=http://localhost:8080
NEXT_PUBLIC_WS_BASE=ws://localhost:8080
```

기본값이 위와 동일해 `.env.local` 없이도 로컬 개발 동작. `.env.example`을 참고로 둔다. 두 키 모두 `NEXT_PUBLIC_*` 접두사 — 백엔드 인증이 없어 비밀 값은 없다.

## 폴더 구조

```
apps/web/
├── CLAUDE.md                  # 프로젝트 헌법 (작업/커밋 전 반드시 정렬)
├── DESIGN.md                  # 디자인 시스템 토큰 카탈로그
├── docs/                      # 학습 노트 (카테고리별)
├── public/
└── src/
    ├── app/
    │   ├── layout.tsx                  # 폰트 + Providers
    │   ├── page.tsx                    # /rooms/lobby로 redirect
    │   └── rooms/[roomId]/
    │       ├── page.tsx                # 채팅 + inspector grid shell
    │       └── page.css.ts
    ├── features/
    │   ├── chat/                       # ChatPanel / MessageList / ChatComposer / SessionBadge / SessionEditModal
    │   └── inspector/                  # InspectorPanel / InspectorHeader / InspectorLine
    ├── lib/
    │   ├── api/                        # REST 클라이언트 + 채널/메시지 호출 (Zod parse)
    │   ├── ws/                         # WsClient 단일 모듈 + Zod schemas + useChatSocket 훅
    │   ├── commands/                   # 슬래시 커맨드 parser / registry / handlers
    │   ├── inspector/                  # inspector 글로벌 emit-subscribe bus
    │   ├── env.ts                      # Zod로 검증된 환경 변수 단일 export
    │   └── id.ts                       # sessionId 생성 + localStorage
    ├── stores/                         # Zustand (sessionStore / inspectorStore)
    ├── hooks/                          # 공통 훅 (useEnsureChannel / useChannelHistory)
    ├── providers/                      # QueryProvider 등 합성
    ├── styles/                         # tokens.ts (SSOT) / theme.css.ts / globals.css.ts
    └── types/                          # api.ts / domain.ts / ws.ts / inspector.ts (외부 의존성 0)
```

각 폴더의 책임과 도입 근거는 [CLAUDE.md](CLAUDE.md) 5번 항목 참조.

## 슬래시 커맨드

채팅 입력창에 `/`로 시작하면 커맨드 모드. 등록되지 않은 커맨드는 inspector에 `[unknown command]` 라인이 출력될 뿐 채팅에 송신되지 않는다.

| 커맨드        | 별칭  | 동작                                       |
| ------------- | ----- | ------------------------------------------ |
| `/inspect`    | `/ws` | inspector 패널 토글                        |
| `/clear`      | —     | inspector 라인 비움 (채팅 영향 없음)       |
| `/disconnect` | —     | WebSocket 강제 종료, 재연결 비활성         |
| `/help`       | —     | 사용 가능한 커맨드 목록을 inspector에 출력 |

새 커맨드는 [`src/lib/commands/registry.ts`](src/lib/commands/registry.ts) 한 곳에 등록 + [`src/lib/commands/handlers/`](src/lib/commands/handlers/)에 핸들러. 전체 데이터 흐름은 [docs/70-architecture/00-slash-command-flow.md](docs/70-architecture/00-slash-command-flow.md).

## 백엔드 통신 서머리

서버: `http://localhost:8080`

- **WebSocket**: `ws://localhost:8080/ws/chat?roomId={roomId}&sessionId={sessionId}` (둘 다 필수, blank 금지)
- **Client → Server (1종)**: `SEND_MESSAGE { type, payload }`
- **Server → Client (3종)**: `CONNECTED` / `CHAT_MESSAGE` / `ERROR`
- **REST (3개)**:
  - `POST /api/v1/channels` (생성 또는 보장)
  - `GET /api/v1/channels/{roomId}`
  - `GET /api/v1/channels/{roomId}/messages?limit=50`
- **인증 없음**. sessionId는 프론트가 정한다. 중복 방지도 프론트 책임.
- OpenAPI: `http://localhost:8080/v3/api-docs`, Swagger UI: `/swagger-ui.html`

세부 계약은 [CLAUDE.md](CLAUDE.md) 4번 항목과 모노레포 root의 [docs/websocket-api.md](../../docs/websocket-api.md) / [docs/realtime-protocol-v1.md](../../docs/realtime-protocol-v1.md).

## 커밋 컨벤션

- `feat` / `fix` / `refactor` / `chore` / `design` / `style` / `docs` / `test` / `!HOTFIX` / `!BREAKING CHANGE`
- 스코프는 코드 영역명.
  - 예: `feat(ws)`, `fix(type)`, `design(tokens)`, `style(lint)`
- `design` = 시각 변화 있음, `style` = 코드 스타일(lint/format 등 시각 변화 없음)
- 매 의미 있는 변경마다 [`docs/`](docs/)에 대응 학습 노트를 동반.

## 학습 문서

`apps/web/docs/`는 본 프로젝트의 구현/결정 학습 기록.

```
docs/
├── README.md                                  # 인덱스
├── _template/                                 # learning-note.md / adr.md
├── 00-project-setup/                          # 의존성 결정, env, eslint
├── 10-nextjs/                                 # RSC vs Client, next/font
├── 20-react/                                  # Context vs Zustand
├── 30-typescript/                             # Zod 패턴
├── 40-websocket/                              # lifecycle, 재연결, StrictMode 더블 마운트
├── 50-styling/                                # vanilla-extract, 토큰 매핑, CSS-in-TS
├── 60-state-data/                             # TanStack Query, 서버/클라이언트 상태 분리
├── 70-architecture/                           # 슬래시 커맨드 흐름
└── 90-decisions/                              # ADR
```

전체 목록과 한 줄 설명은 [docs/README.md](docs/README.md).

## 관련 문서

- [CLAUDE.md](CLAUDE.md) — 프로젝트 헌법 (작업/커밋 전 반드시 참조)
- [DESIGN.md](DESIGN.md) — 디자인 시스템 토큰 카탈로그
- [docs/README.md](docs/README.md) — 학습 노트 인덱스
- [모노레포 README](../../README.md) — 전체 구조와 backend/api 연동
- [docs/frontend-backend-contract.md](../../docs/frontend-backend-contract.md) — 프론트/백 공유 계약
