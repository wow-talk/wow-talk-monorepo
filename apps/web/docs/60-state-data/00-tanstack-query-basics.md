# 00. TanStack Query 기초 (2026-05-20)

> TanStack Query v5로 REST 호출을 캐싱·재시도·동기화한다. 채널 보장은 `useMutation`, 메시지 히스토리는 `useQuery`. QueryClient는 layout에서 합성.

## 어디서 (Where)

- `src/providers/QueryProvider.tsx` — QueryClient 생성 + Provider + DevTools
- `src/hooks/useEnsureChannel.ts` — useMutation 예시
- `src/hooks/useChannelHistory.ts` — useQuery 예시
- `src/app/layout.tsx` — `<QueryProvider>`로 children 감쌈

## 무엇을 (What)

### 1) QueryProvider 합성

```tsx
"use client";

export function QueryProvider({ children }: { children: ReactNode }) {
  const [client] = useState(() => new QueryClient({
    defaultOptions: { queries: { staleTime: 30_000, gcTime: 5 * 60_000, retry: 1, refetchOnWindowFocus: false } }
  }));
  return (
    <QueryClientProvider client={client}>
      {children}
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}
```

핵심:

- `"use client"` — Provider는 Context API 기반이라 Client Component.
- `useState`로 QueryClient를 컴포넌트 라이프사이클에 묶어둠. **이게 SSR/CSR 경계 안전성의 핵심**. 모듈 스코프에 `new QueryClient()` 두면 서버 인스턴스가 전체 요청에서 공유돼 사용자 간 캐시 누수 위험.
- `defaultOptions.queries`:
  - `staleTime: 30s` — 30초 동안은 "fresh"로 간주, 재페치 안 함.
  - `gcTime: 5m` — 비활성 캐시를 5분 후 garbage collect.
  - `retry: 1` — 실패 시 한 번 재시도 (네트워크 깜빡임 대비).
  - `refetchOnWindowFocus: false` — 탭 전환마다 fetch 폭주 막음. 채팅은 WS로 실시간이라 필요 없음.

### 2) useQuery — 메시지 히스토리

```ts
useQuery({
  queryKey: ["channel", roomId, "messages", limit],
  queryFn: () => getChannelMessages(roomId, limit),
  enabled: Boolean(roomId),
});
```

- **queryKey**는 캐시의 "주소". 배열 형태가 표준. `roomId`나 `limit`이 바뀌면 자동으로 새 fetch.
- `enabled` false면 query 정지 — `roomId`가 아직 없을 때 fetch 막음.
- 반환값 핵심 필드:
  - `data` — fetch 성공 시의 응답
  - `isPending` — 첫 fetch가 진행 중 (v5에서 `isLoading`이 이걸로 사실상 대체됨)
  - `isFetching` — 첫 fetch든 background refetch든 fetch 중일 때 true
  - `error` — 마지막 실패 (Error 객체)
  - `refetch()` — 수동 재페치

### 3) useMutation — 채널 보장

```ts
useMutation({
  mutationFn: (roomId: RoomId) => ensureChannel(roomId),
});
```

- mutation은 "사용자 행동에 따른 일회성 트리거" 의도.
- `mutate(roomId)` 또는 `mutateAsync(roomId)`로 발사.
- `useQuery`처럼 캐시되지 않음 — 매번 실행.
- 반환값: `mutate`, `isPending`, `isSuccess`, `error`, `data`.

`ensureChannel`은 사실상 idempotent(여러 번 호출해도 같은 결과)지만, "방 입장 시 한 번 실행"이라는 의도 표현에는 mutation이 자연스럽다. useQuery로 짜도 동작은 함.

## 왜 (Why)

### 1) 왜 직접 fetch + useState로 안 짜는가

직접 짜면:
- 캐시 없음 (같은 데이터를 컴포넌트 마운트마다 재페치)
- 재시도/로딩/에러 상태를 매번 손으로 관리
- 같은 데이터 두 컴포넌트가 쓰면 동기화 코드 직접 작성

TanStack Query가 위 셋을 표준 패턴으로 해결.

### 2) v5의 변경점 — `isLoading` vs `isPending`

v4에서는 `isLoading`이 "첫 페치 중"을 의미했다. v5에서는 의미가 좁아져 `isLoading`은 거의 안 쓰고, **`isPending`**이 표준이다. "데이터가 아직 없고 받는 중"이면 `isPending`, "data는 있는데 백그라운드로 새로고침 중"이면 `isFetching`만 true.

UI 표현:
- 첫 로딩 스피너: `isPending`
- 살짝 깜빡이는 background spinner: `isFetching` (data는 이미 표시 중)

### 3) queryKey 설계 원칙

- 배열로 작성. 첫 요소는 도메인("channel"), 그 뒤는 식별자(roomId), 그 뒤는 sub-리소스("messages"), 그 뒤는 옵션(limit).
- **불변값만 사용** — 함수, 객체 인스턴스 금지(매번 새로 만들면 같은 데이터인데 매번 새 fetch).
- 캐시 무효화 시 prefix 매칭이 가능 — `queryClient.invalidateQueries({ queryKey: ["channel", roomId] })`로 그 채널의 모든 sub-쿼리 한 번에 갱신.

## Before / After

### Before — 수동 fetch

```tsx
const [messages, setMessages] = useState<ChatMessageHistory>([]);
const [isLoading, setIsLoading] = useState(true);
useEffect(() => {
  setIsLoading(true);
  getChannelMessages(roomId).then((data) => {
    setMessages(data);
    setIsLoading(false);
  });
}, [roomId]);
```

문제: 캐시 없음, 에러 핸들링 누락, 재시도 없음, 두 컴포넌트가 쓰면 두 번 fetch.

### After — useQuery

```ts
const { data: messages = [], isPending } = useChannelHistory(roomId);
```

위 문제 모두 라이브러리가 해결. 같은 `queryKey`는 자동 dedupe.

## 장단점 (Trade-offs)

- **채택안: TanStack Query v5**
  - 장점: 캐시/재시도/dedupe/DevTools 표준, 채용 시장 인지도 높음.
  - 단점: 학습 곡선(특히 staleTime vs gcTime, queryKey 설계, v5 변경점), 번들 추가.
- **미채택안: SWR**
  - 장점: Next 친화, 더 가벼움, API 단순.
  - 단점: mutation/낙관적 업데이트는 TanStack Query가 더 풍부, 시장 점유율 더 낮음.

## 영향 (Impact)

- 컴포넌트가 더 선언적. fetch 코드 사라짐.
- DevTools로 query 상태(fresh/stale/fetching/idle)를 시각화 → 디버깅 비용 큰 폭 절감.
- 같은 데이터를 여러 컴포넌트가 써도 1회 fetch.

## 더 읽을거리 (Refs)

- 공식: https://tanstack.com/query/v5/docs/framework/react/overview
- v5 마이그레이션 가이드: https://tanstack.com/query/v5/docs/framework/react/guides/migrating-to-v5
- queryKey 설계: https://tkdodo.eu/blog/effective-react-query-keys
- 관련 노트: [[01-zod-patterns]]
