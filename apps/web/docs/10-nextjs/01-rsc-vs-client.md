# 01. RSC vs Client Component (2026-05-20)

> Next.js App Router는 기본이 React Server Component(RSC). 상태/이펙트/브라우저 API가 필요한 컴포넌트만 `"use client"`로 마킹해 Client Component로 전환한다. 본 프로젝트의 컴포넌트 대부분이 Client Component인 이유와 layout/page 경계 결정의 근거.

## 어디서 (Where)

- `src/app/layout.tsx` — Server Component (Provider 합성만 위임)
- `src/app/page.tsx` — Server Component (redirect만)
- `src/app/rooms/[roomId]/page.tsx` — Client Component (`"use client"`)
- `src/providers/QueryProvider.tsx` — Client Component (Context API 기반)
- `src/features/chat/*` — 거의 모두 Client Component

## 무엇을 (What)

### 1) RSC와 Client Component의 차이

| 구분 | RSC (기본) | Client Component (`"use client"`) |
|---|---|---|
| 실행 위치 | 서버에서만 | 서버에서 SSR + 클라이언트에서 hydration |
| `useState` / `useEffect` | 불가 | 가능 |
| 브라우저 API (`window`, `localStorage`) | 불가 | 가능 (effect 안에서) |
| 이벤트 핸들러 (`onClick` 등) | 불가 | 가능 |
| 번들 사이즈 | 0 (JS로 안 나감) | 포함됨 |
| 직접 데이터 fetch | 가능 (서버 fetch) | 권장 안 함 |

### 2) `"use client"` 지시어 동작

파일 최상단에 `"use client"`를 두면 그 파일과 **그 파일이 import한 모든 컴포넌트**가 클라이언트 번들에 포함된다. 즉 경계는 "use client" 파일에서 시작.

```tsx
// QueryProvider.tsx
"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
// 이 파일과 children은 클라이언트로.
```

### 3) layout/page의 경계 결정

- `layout.tsx` — 기본 RSC. 정적 `<html>`, `<body>`, `metadata`만 두기. Provider 합성은 자식 Client Component로 위임.
- `page.tsx`(루트) — RSC. `redirect()`만 호출.
- `rooms/[roomId]/page.tsx` — Client Component. WebSocket / 로컬 상태가 핵심.

`QueryProvider`를 별도 Client Component로 뺀 이유: layout을 RSC로 유지하면서 그 안에서 Context Provider 사용 가능.

### 4) `use(params)` — Next 15+의 비동기 params

```tsx
export default function RoomPage({ params }: { params: Promise<{ roomId: string }> }) {
  const { roomId } = use(params);
  // ...
}
```

Next 15부터 dynamic route의 `params`가 **Promise**로 변경. `use()`로 unwrap. Client Component에서도 동일.

## 왜 (Why)

### 1) 왜 layout을 RSC로 유지하는가

- 폰트 로드(`next/font`), metadata, `<html>` lang 등은 서버에서 정해지면 첫 페인트 안정.
- layout 자체를 Client로 만들면 그 트리 전체가 클라이언트로 끌려가 RSC의 이점(번들 0)이 사라짐.
- 결과: layout은 RSC, Provider는 Client Component로 분리.

### 2) 왜 채팅 페이지는 Client Component인가

- WebSocket, localStorage(sessionId), Zustand 구독, TanStack Query 모두 클라이언트 전용.
- 페이지 전체를 Client Component로 표시하는 게 단순. RSC + 부분 Client 분리는 채팅처럼 인터랙티브가 80% 이상인 화면에서는 오버 엔지니어링.

### 3) hydration mismatch 회피

`sessionStore`의 초기값은 빈 문자열이고, `hydrate()`가 effect에서 localStorage를 읽는다. 만약 store 초기값이 localStorage 의존이면 **서버 렌더(빈 값)**와 **클라이언트 렌더(localStorage 값)**가 달라 hydration mismatch 에러. 빈 값으로 시작 → effect로 적재 → 적재 전 까지 `return null` 패턴으로 회피.

## Before / After

### Before — 모든 페이지를 RSC로 짜려 시도

```tsx
// page.tsx (RSC)
export default async function RoomPage({ params }: { params: { roomId: string } }) {
  const data = await fetch(...);
  return <ChatPanel data={data} />;
}
```

문제:
- WebSocket은 RSC에서 못 만듦.
- TanStack Query Provider도 Context라 Client 필요.
- 결국 `<ChatPanel>`을 Client로 만들어야 하는데 props로 데이터 직렬화/전달 부담.

### After — 채팅 페이지 전체를 Client로

```tsx
"use client";

export default function RoomPage({ params }: { params: Promise<{ roomId: string }> }) {
  const { roomId } = use(params);
  // ... 모든 hook 자유롭게
  return <ChatPanel roomId={roomId} />;
}
```

장점: 단순. WebSocket/Zustand/TanStack Query를 자연스럽게 사용.
단점: 페이지 전체가 클라이언트 번들. 채팅처럼 인터랙티브 비중이 크면 합당.

## 장단점 (Trade-offs)

- **채택안: layout/redirect만 RSC, 나머지는 Client**
  - 장점: 단순한 멘탈 모델, hydration 안전, Provider 패턴 명확.
  - 단점: 채팅 페이지 번들 사이즈가 RSC 분리보다 약간 큼(현실적 무시 가능).
- **미채택안: RSC + 부분 Client (fine-grained)**
  - 장점: 번들 최적.
  - 단점: 메시지 리스트만 Client로 분리하기 어려움(상태 공유 복잡). 채팅 데모엔 과한 최적화.

## 영향 (Impact)

- 첫 페인트는 RSC가 그린 정적 layout + 로딩 placeholder → 클라이언트 hydration 후 인터랙티브.
- `metadata`가 RSC에서 정해져 초기 HTML에 포함 → SEO/탭 타이틀 안정.

## 더 읽을거리 (Refs)

- React Server Components: https://react.dev/reference/rsc/server-components
- Next App Router: https://nextjs.org/docs/app/building-your-application/rendering
- 비동기 params: https://nextjs.org/docs/app/api-reference/file-conventions/page#async-params
- 관련 노트: [[00-tanstack-query-basics]], [[01-context-vs-zustand]]
