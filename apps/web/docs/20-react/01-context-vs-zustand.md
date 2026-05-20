# 01. Context API vs Zustand (2026-05-20)

> 같은 sessionId를 여러 컴포넌트(MessageItem, ChatPanel, SessionBadge, SessionEditModal)가 공유해야 한다. Context API와 Zustand 두 방식의 차이를 코드로 비교.

## 어디서 (Where)

- `src/stores/sessionStore.ts` — Zustand 구현(채택)
- 같은 시나리오를 Context로 구현했다면 어떻게 됐을지 이 노트에서 코드로 대조

## 무엇을 (What)

### 시나리오

- `sessionId`(현재 사용자 별명)는 다음 컴포넌트가 모두 알아야 함:
  - `SessionBadge` — 헤더에 표시
  - `SessionEditModal` — 편집
  - `MessageItem` — 본인 메시지 여부 판정
  - `useChatHistory` 훅 — WS 연결 인자
- 변경 빈도는 낮음(사용자가 모달을 통해 가끔). 하지만 `MessageItem`이 메시지 수십~수백 개라 re-render 효율이 중요.

### 1) Context API 구현 (가상의 비채택안)

```tsx
// SessionContext.tsx
"use client";

import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { getOrCreateSessionId, storeSessionId } from "@/lib/id";

interface SessionContextValue {
  sessionId: string;
  setSessionId: (v: string) => void;
}

const SessionContext = createContext<SessionContextValue | null>(null);

export function SessionProvider({ children }: { children: ReactNode }) {
  const [sessionId, setLocalSessionId] = useState("");
  useEffect(() => { setLocalSessionId(getOrCreateSessionId()); }, []);
  const setSessionId = (v: string) => {
    const t = v.trim();
    if (!t) return;
    storeSessionId(t);
    setLocalSessionId(t);
  };
  return (
    <SessionContext.Provider value={{ sessionId, setSessionId }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error("useSession outside SessionProvider");
  return ctx;
}
```

사용 측:

```tsx
const { sessionId } = useSession();
```

문제:

1. **모든 구독자 re-render**: `setSessionId` 호출 시 `SessionContext.Provider`의 value 객체가 새로 만들어지고, `useContext(SessionContext)`를 호출한 **모든** 컴포넌트가 re-render. `MessageItem`이 100개 있으면 100개 다 다시 그림.
2. **Provider 위치 결정 부담**: 어느 layout에 두느냐가 트리 범위 결정. 깊으면 다른 곳에서 못 씀, 얕으면 SSR/RSC 경계 이슈.
3. **값 객체 메모이제이션 의무**: 위 코드에서 `value={{ sessionId, setSessionId }}`는 매 render마다 새 객체. `useMemo` 빠뜨리면 자식이 매번 re-render.

### 2) Zustand 구현 (채택)

```ts
// sessionStore.ts
"use client";

import { create } from "zustand";
import { getOrCreateSessionId, storeSessionId } from "@/lib/id";

interface SessionState {
  sessionId: string;
  hydrated: boolean;
  hydrate: () => void;
  setSessionId: (v: string) => void;
}

export const useSessionStore = create<SessionState>((set) => ({
  sessionId: "",
  hydrated: false,
  hydrate: () => set({ sessionId: getOrCreateSessionId(), hydrated: true }),
  setSessionId: (v: string) => {
    const t = v.trim();
    if (!t) return;
    storeSessionId(t);
    set({ sessionId: t });
  },
}));
```

사용 측:

```tsx
const sessionId = useSessionStore((s) => s.sessionId);
const setSessionId = useSessionStore((s) => s.setSessionId);
```

장점:

1. **Selector 단위 구독**: `useSessionStore((s) => s.sessionId)`는 `sessionId`가 바뀔 때만 re-render. `setSessionId`만 쓰는 컴포넌트는 sessionId 변경에 영향 없음.
2. **Provider 없음**: store는 모듈 스코프 객체. 어디서든 import해서 사용. SSR/RSC 경계 고려 최소(단, store 정의 파일은 Client).
3. **selector 누락도 안전**: 객체로 받으면 `(s) => s`로 전체 구독해도 동작은 함. 단 re-render 최적화 위해 selector 권장.

## 왜 (Why)

### 1) 채팅 데모에서는 selector 단위 re-render가 핵심

`MessageItem`이 100개 마운트된 상태에서 `setSessionId`만 호출돼도 Context는 100개 다 re-render. Zustand는 `sessionId`를 구독한 컴포넌트만 re-render(즉 SessionBadge, MessageItem). 그런데 MessageItem도 자기 `message.sessionId`와 비교하므로 어차피 re-render가 의미 있음(본인 여부 재계산). 결국 차이가 미미해 보이지만, "스토어가 늘어났을 때 영향 범위가 명확하다"는 학습 자산이 중요.

### 2) hydration 패턴이 깔끔

Zustand는 모듈 스코프라 SSR 초기값(빈 문자열)을 강제하고, `hydrate()`를 effect에서 한 번 호출. Context도 가능하지만 Provider 어디에서 useEffect를 호출할지 결정해야 함.

### 3) Vue의 Pinia와 익숙함

본 프로젝트 담당자가 Vue 개발자라 Pinia를 써왔다. Pinia의 store와 Zustand의 store는 멘탈 모델이 거의 같다(action으로 state 갱신, 컴포넌트는 store 직접 import). 학습 비용이 가장 낮은 선택.

## Before / After

### Before — Context로 짰을 때

```tsx
function MessageItem({ message }: Props) {
  const { sessionId } = useSession();
  const isMine = message.sessionId === sessionId;
  // ...
}
```

`SessionContext.Provider`의 value가 바뀔 때마다 `useSession()` 호출한 모든 MessageItem이 re-render.

### After — Zustand

```tsx
function MessageItem({ message }: Props) {
  const mySessionId = useSessionStore((s) => s.sessionId);
  const isMine = message.sessionId === mySessionId;
  // ...
}
```

`s.sessionId`가 바뀔 때만 re-render. 다른 store key가 늘어나도(`hydrated`, future `username`) 영향 없음.

## 장단점 (Trade-offs)

- **채택안: Zustand**
  - 장점: selector 단위 구독, Provider 없음, Pinia 경험 전이, 학습 곡선 낮음.
  - 단점: 모듈 스코프라 SSR에서 사용자 간 공유 사고 가능(단, 본 프로젝트는 클라이언트 전용 사용이라 무관).
- **미채택안: Context API**
  - 장점: React 내장, 별도 의존성 0.
  - 단점: 전체 구독자 re-render, value 객체 메모이제이션 의무, Provider 위치 결정 부담.
- **미채택안: useReducer + Context**
  - 장점: dispatch 패턴이 명확.
  - 단점: 작은 store에 과함. 별도 노트(`02-usereducer-vs-zustand.md`)에서 더 깊이 비교 예정.

## 영향 (Impact)

- 메시지 수 증가에도 re-render 비용 안정.
- Provider tree 단순.
- 추후 store 추가(inspector 토글, theme 등) 시 같은 패턴으로 확장.

## 더 읽을거리 (Refs)

- Zustand 공식: https://zustand.docs.pmnd.rs
- Context re-render 문제: https://tkdodo.eu/blog/use-state-vs-use-reducer
- 관련 노트: [[02-usereducer-vs-zustand]] (다음 라운드 예정), [[00-tanstack-query-basics]]
