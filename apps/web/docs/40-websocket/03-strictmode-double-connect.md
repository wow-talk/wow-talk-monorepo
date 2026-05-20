# 03. StrictMode 더블 마운트와 WebSocket (2026-05-20)

> React 18+ StrictMode는 개발 모드에서 effect를 두 번 호출한다(mount → cleanup → mount). WebSocket을 effect로 만들면 100% 두 번 연결 후 하나는 즉시 닫히는 사고. 클린업을 정확히 작성하면 안전.

## 어디서 (Where)

- `src/lib/ws/useChatSocket.ts` — useEffect 클린업에서 `client.dispose()` 호출
- `src/lib/ws/wsClient.ts` — `dispose()`가 listener 정리 + reconnect 비활성 + socket close

## 무엇을 (What)

### 1) StrictMode가 effect를 두 번 부르는 이유

```tsx
// React StrictMode (dev only):
// 1. 첫 마운트: effect 실행
// 2. 즉시 cleanup
// 3. 다시 마운트: effect 재실행
```

목적: cleanup 함수가 누수 없이 작성됐는지 강제 검증. cleanup이 listener 해제/connection close를 빠뜨리면 두 번째 마운트 후 두 개의 연결이 떠다닌다.

### 2) WebSocket 사고 패턴 (잘못된 코드)

```tsx
useEffect(() => {
  const ws = new WebSocket(url);
  ws.onmessage = (e) => setMessages(prev => [...prev, e.data]);
  // 클린업 없음! 또는 ws.close()만 호출
}, [url]);
```

문제:
- 첫 mount에서 ws1 생성.
- StrictMode가 cleanup 호출 → cleanup이 없으면 ws1은 떠다닌다.
- 두 번째 mount에서 ws2 생성.
- 결과: 두 개 연결이 같은 sessionId로 떠다님 → 백엔드가 sessionId 중복으로 거부하거나 메시지 두 번 받음.

### 3) 본 프로젝트의 안전 패턴

```tsx
useEffect(() => {
  if (!enabled || !roomId.trim() || !sessionId.trim()) return;

  const client = new WsClient();
  clientRef.current = client;
  setLiveMessages([]);

  const unsubscribe = client.on((event) => { /* ... */ });
  client.connect({ roomId, sessionId });

  return () => {
    unsubscribe();
    client.dispose();          // listener 정리 + reconnect 비활성 + socket close
    clientRef.current = null;
  };
}, [roomId, sessionId, enabled, appendLog]);
```

핵심:
- `client.dispose()`에서 listener 모두 해제 → 두 번째 mount의 listener가 첫 mount의 socket을 받을 일 없음.
- `reconnect = false` 설정 → close 후 재연결 시도 안 함.
- `closeSocket()`에서 socket의 핸들러를 null로 한 뒤 close → close 도중 onclose가 두 번 발화하는 사고 방지.

## 왜 (Why)

### 1) Ref + Effect 패턴이 가장 안정적

instance를 모듈 스코프나 useState에 두지 않고 `useRef` + effect 내 생성:

- 모듈 스코프: SSR에서 모듈 로드만으로 connection 시도, 사용자 간 공유 위험.
- useState: state 변경마다 re-render. WsClient 자체는 mutable 객체라 state로 관리할 필요 없음.
- **useRef + effect 내 생성**: 정확히 mount 시점에 생성, unmount 시점에 정리. ref는 re-render 트리거 안 함.

### 2) appendLog를 deps에 넣는 이유

```tsx
const appendLog = useCallback(..., []);
useEffect(() => { ... }, [roomId, sessionId, enabled, appendLog]);
```

ESLint react-hooks/exhaustive-deps가 권장. `appendLog`는 `useCallback`으로 stable 참조라 deps에 넣어도 effect 재실행 안 함. 빠뜨리면 lint 경고.

### 3) `setLiveMessages([])`를 effect 입구에 두는 이유

roomId나 sessionId가 변경되면 이전 룸/세션의 메시지가 화면에 남으면 안 됨. effect는 둘 중 하나라도 변경되면 cleanup + 재실행이라, 입구에서 비워준다.

## Before / After

### Before — 사고 패턴

```tsx
useEffect(() => {
  const ws = new WebSocket(url);
  ws.onmessage = (e) => setMessages((prev) => [...prev, JSON.parse(e.data)]);
  return () => ws.close();
}, [url]);
```

문제:
- close가 비동기. close 도중 onmessage가 두 번 발화 가능.
- listener를 따로 정리 안 함 → 두 번째 mount에서 첫 ws의 onmessage가 여전히 setState 호출.

### After — WsClient + dispose

```tsx
useEffect(() => {
  const client = new WsClient();
  const off = client.on(handler);
  client.connect(params);
  return () => { off(); client.dispose(); };
}, [params]);
```

- `dispose`가 listener clear + reconnect 비활성 + socket의 onopen/onmessage/onerror/onclose를 null로 설정한 뒤 close → 어떤 시점에 close가 실제 완료되든 두 번째 mount에 영향 없음.

## 장단점 (Trade-offs)

- **채택안: useRef + effect 안 생성 + dispose**
  - 장점: StrictMode에서 안전. 메모리 누수 0. 다른 React 사이드이펙트(navigation, hot reload)에서도 동일 동작.
  - 단점: useEffect dependency 정확히 관리해야 함(특히 stable 참조).
- **미채택안: StrictMode 비활성화**
  - 장점: 사고 안 발생.
  - 단점: 다른 누수까지 가려짐, prod에서 다른 트리거(라우팅, hot reload)로 동일 사고 가능.

## 영향 (Impact)

- dev에서 StrictMode로 두 번 mount해도 연결 한 개만 안정 유지.
- prod에서 라우팅으로 컴포넌트가 unmount/mount되어도 lifecycle 안전.
- 학습: "WebSocket은 effect로 만들 때 cleanup을 라이브러리 수준으로 작성해야 한다"는 일반 원칙이 자연스럽게 체득.

## 더 읽을거리 (Refs)

- React 19 StrictMode: https://react.dev/reference/react/StrictMode
- 공식: "You Might Not Need an Effect" — https://react.dev/learn/you-might-not-need-an-effect
- 관련 노트: [[00-ws-lifecycle]]
