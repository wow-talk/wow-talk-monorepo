# 02. 서버 상태 vs 클라이언트 상태 (2026-05-20)

> 같은 React 컴포넌트에서 다루는 "상태"라도 그 출처에 따라 도구를 달리 선택해야 한다. 메시지(서버 진실)는 TanStack Query, sessionId(클라이언트 결정)는 Zustand. 둘을 섞으면 캐시 무효화/동기화 사고가 일어난다.

## 어디서 (Where)

- `src/hooks/useChannelHistory.ts` — 서버 상태(REST 메시지 히스토리, TanStack Query)
- `src/lib/ws/useChatSocket.ts` — 서버 상태(WS 실시간 메시지, React state로 누적)
- `src/stores/sessionStore.ts` — 클라이언트 상태(sessionId, Zustand)
- `src/features/chat/useChatHistory.ts` — 두 출처를 합쳐 단일 배열로 노출

## 무엇을 (What)

### 1) 서버 상태(Server State)란

- **출처가 외부**: 백엔드 DB, WS 서버.
- **여러 클라이언트가 공유** 가능.
- **언제든 갱신** 됨(다른 사용자의 행동).
- 우리는 그저 **로컬에 캐시**해두고, 최신성을 유지하기 위해 주기적으로 다시 묻거나 push(WS)로 받는다.

본 프로젝트의 서버 상태:
- 채널 메타데이터(POST/GET `/api/v1/channels`)
- 메시지 히스토리(GET `/api/v1/channels/:id/messages`)
- 실시간 메시지(WS `CHAT_MESSAGE`)

### 2) 클라이언트 상태(Client State)란

- **출처가 이 클라이언트 본인**: 사용자 입력, UI 토글, 로컬 식별자.
- 다른 클라이언트와 공유 안 함(서버는 모름).
- 백엔드 동기화 무관.

본 프로젝트의 클라이언트 상태:
- `sessionId`(별명) — 프론트가 정함, localStorage 영속
- 모달 열림/닫힘
- 입력 중인 텍스트(`ChatComposer`의 useState)
- 슬래시 커맨드 inspector 토글(Phase 2)

### 3) 도구 매핑

| 상태 종류 | 도구 | 본 프로젝트 예시 |
|---|---|---|
| 서버 상태 (REST) | TanStack Query | `useChannelHistory` |
| 서버 상태 (실시간) | React state + WS 라이브러리 | `useChatSocket.liveMessages` |
| 클라이언트 글로벌 | Zustand | `useSessionStore` |
| 클라이언트 로컬 | `useState` | `ChatComposer`의 `text` |

### 4) 합치는 패턴

```ts
// useChatHistory
const history = useChannelHistory(roomId);          // 서버 상태 (REST 캐시)
const socket = useChatSocket({ roomId, sessionId }); // 서버 상태 (WS 누적)

const messages = useMemo(() => {
  const rest = history.data ?? [];
  const lastRestTime = rest[rest.length - 1]?.sentAt;
  const newer = socket.liveMessages.filter(m => !lastRestTime || m.sentAt > lastRestTime);
  return [...rest, ...newer];
}, [history.data, socket.liveMessages]);
```

REST는 mount 시 한 번 캐시. WS는 그 이후 들어오는 것만 뒤에 붙임 → 자연스러운 시간순.

## 왜 (Why)

### 1) 둘을 같은 도구로 다루면 안 되는 이유

만약 메시지도 Zustand에 쌓는다면:

```ts
// 안티패턴 가정
const messages = useChatStore((s) => s.messages);
// REST 응답을 store에 push, WS도 push
```

문제:
- TanStack Query의 캐시/dedupe/재시도 못 씀.
- 다른 컴포넌트가 같은 데이터 필요할 때 또 fetch.
- queryKey로 prefix 무효화 같은 표준 패턴 부재.

만약 sessionId를 TanStack Query에 넣으면:
- queryKey가 의미 없음(이 클라이언트만 아는 값).
- staleTime 같은 개념이 부조리.

### 2) 진실의 출처(Source of Truth) 분리

- 서버 상태의 진실은 백엔드. 우리는 캐시 + 동기화.
- 클라이언트 상태의 진실은 우리. 백엔드는 모름.

이 구분이 명확해야 "사용자가 메시지 입력 → composer state(클라) → send() → WS push → 서버 진실 → CHAT_MESSAGE 브로드캐스트 → 우리도 다시 받음(서버 상태)"라는 사이클을 깨지 않는다. 본인 메시지를 클라에서 즉시 messages에 push하면 서버에서 받은 메시지와 중복 발생.

### 3) 그래서 본 프로젝트의 송신 흐름

```
ChatComposer 입력 → send() → WS push → (서버) → CHAT_MESSAGE 브로드캐스트 → 발신자 본인도 수신 → liveMessages에 누적 → messages 배열에 반영
```

본인 메시지를 클라에서 즉시 push하지 않는다. 서버 진실을 기다린다 → 본인/타인 동일 경로 → 코드 단순.

## Before / After

### Before — 모든 걸 단일 store로

```ts
// 안티패턴
const useChatStore = create((set) => ({
  messages: [],
  sessionId: "",
  addMessage: (m) => set((s) => ({ messages: [...s.messages, m] })),
  setSessionId: (v) => set({ sessionId: v }),
  loadHistory: async (roomId) => { /* fetch + set */ },
}));
```

문제:
- loadHistory가 직접 fetch — 캐시 없음.
- 두 컴포넌트가 같은 roomId 띄우면 두 번 fetch.
- 재시도/staleTime/refetchOnWindowFocus 직접 구현.

### After — 도구 분리

```ts
// 서버 상태
const history = useChannelHistory(roomId);          // TanStack Query
const socket = useChatSocket({ roomId, sessionId }); // WS + React state

// 클라이언트 상태
const sessionId = useSessionStore((s) => s.sessionId); // Zustand
```

각 도구의 표준 패턴 그대로 사용.

## 장단점 (Trade-offs)

- **채택안: 출처별 도구 분리**
  - 장점: 각 도구의 best practice 그대로 적용, 디버깅(DevTools) 분리.
  - 단점: 합치는 hook(useChatHistory) 한 번 더 작성. 그게 정상.
- **미채택안: Redux 같은 단일 store에 다 넣기**
  - 장점: 한 군데에서 모든 상태 확인.
  - 단점: 서버 상태의 캐시 메타(stale/fetching/error)를 직접 표현해야 함 — RTK Query를 쓰면 결국 도구 분리.

## 영향 (Impact)

- 메시지 갱신 시 sessionId 모달 등은 영향 없음(scope 격리).
- 같은 roomId 보는 두 컴포넌트는 1회 fetch.
- 코드 read 시 "이건 어디서 오는 상태인가"가 import만 보고도 분간 가능.

## 더 읽을거리 (Refs)

- TKDodo, "Server State vs Client State": https://tkdodo.eu/blog/practical-react-query
- Tanner Linsley, "It's Not Global State": https://twitter.com/tannerlinsley
- 관련 노트: [[00-tanstack-query-basics]], [[01-context-vs-zustand]]
