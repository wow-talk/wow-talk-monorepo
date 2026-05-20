# 00. WebSocket 라이프사이클 (2026-05-20)

> 브라우저 WebSocket의 readyState 4단계와 이벤트 4개, 정상/비정상 close의 구분, 재연결 정책을 단일 모듈(`WsClient`)에 가둔다. 컴포넌트는 라이프사이클의 어떤 디테일도 몰라도 된다.

## 어디서 (Where)

- `src/lib/ws/wsClient.ts` — WebSocket을 소유하는 클래스
- `src/lib/ws/schemas.ts` — 수신 메시지 Zod 스키마
- `src/lib/ws/useChatSocket.ts` — React 훅 래퍼
- `src/lib/id.ts` — sessionId 생성/영속

## 무엇을 (What)

### 1) readyState 4단계

| 값 | 상수 | 의미 |
|---|---|---|
| 0 | `WebSocket.CONNECTING` | 핸드셰이크 중 |
| 1 | `WebSocket.OPEN` | 메시지 송수신 가능 |
| 2 | `WebSocket.CLOSING` | 닫는 중 (`close()` 호출 후 핸드셰이크 완료 전) |
| 3 | `WebSocket.CLOSED` | 닫힘 또는 연결 실패 |

`send()`는 OPEN일 때만 가능. 다른 상태에서 호출하면 throw하거나 silent fail(브라우저별로 다름) → `WsClient.send`는 readyState 체크 후 boolean 반환으로 호출 측에 알려준다.

### 2) 이벤트 4개

| 이벤트 | 발화 시점 |
|---|---|
| `open` | CONNECTING → OPEN 전이 |
| `message` | 서버에서 한 프레임 도착 |
| `error` | 전송 실패, 비정상 종료 등. 정보가 거의 없는 일반 Event 객체 |
| `close` | CLOSED 진입. `event.wasClean`, `event.code`, `event.reason` 제공 |

**중요**: `error` 다음에는 `close`가 거의 항상 따라온다. 재연결 로직은 `close`에만 두는 게 깔끔. `error`는 status 표시용으로만.

### 3) 정상 close vs 비정상 close

- **정상**: `event.wasClean === true`. 보통 코드 1000(`NORMAL_CLOSURE`) 또는 1001(`GOING_AWAY`). 사용자가 페이지를 떠나거나 `close()`를 명시적으로 호출했을 때.
- **비정상**: `event.wasClean === false`. 코드 1006(`ABNORMAL_CLOSURE`)이 대표. 네트워크 단절, 서버 다운, 핸드셰이크 실패 등. **이때만 재연결 시도**.

### 4) 본 프로젝트의 재연결 정책 (v0)

- 지수 백오프: 500ms / 1s / 2s / 4s / 8s
- 최대 5회 시도. 이후 멈춤(사용자가 명시 재연결 트리거 필요)
- `wsClient.disconnect()` 호출 후에는 재연결 안 함(`reconnect` 플래그 false)
- 모든 시도 횟수/지연/원인은 inspector(또는 콘솔)에 로깅

### 5) WsClient 외부 인터페이스

```ts
const client = new WsClient();
const unsubscribe = client.on((event) => {
  // event.type: "status" | "message" | "parse-error" | "retry"
});
client.connect({ roomId, sessionId });
client.send({ type: "SEND_MESSAGE", payload: "hi" });
client.disconnect();
client.dispose(); // 모든 listener 정리 + 재연결 비활성
unsubscribe();
```

emit-subscribe 패턴이라 같은 클라이언트를 여러 컴포넌트가 구독해도 안전.

## 왜 (Why)

### 1) 단일 모듈로 가둔 이유

WebSocket을 컴포넌트가 직접 만들면:

- 같은 페이지에 여러 컴포넌트가 있으면 각자 연결 → 서버 부담 + 메시지 중복
- 컴포넌트 unmount/remount마다 새 연결 → StrictMode 더블 마운트에서 100% 사고
- 재연결 정책을 컴포넌트마다 재작성

`WsClient` 인스턴스 한 개를 hook이 effect에 묶어 소유. 컴포넌트는 hook만 본다.

### 2) Zod parse를 수신 시점에 두는 이유

서버 계약이 변경되거나 새 type이 추가되면 알 수 없는 형식이 들어온다. 캐스팅으로 받으면 화면 어딘가에서 `undefined`가 흘러다니다 사고. 수신 직후 `safeParse` 실패 → 채팅 UI에 노출하지 않고 inspector에만 ERROR 로깅 → 디버깅 즉시 가능.

### 3) onclose에 핸들러 정리 후 close

`closeSocket()`에서 핸들러를 null로 끄고 close 호출:

```ts
s.onopen = null; s.onmessage = null; s.onerror = null; s.onclose = null;
s.close();
```

이렇게 안 하면 close 중 onclose가 한 번 더 발화해 retry 로직이 두 번 실행되는 사고가 일어난다.

## Before / After

### Before — 컴포넌트가 직접 연결

```tsx
useEffect(() => {
  const ws = new WebSocket(`${base}/ws/chat?roomId=${room}&sessionId=${id}`);
  ws.onmessage = (e) => { /* JSON.parse + setState */ };
  return () => ws.close();
}, [room, id]);
```

문제: 재연결 없음, 파싱 검증 없음, 다른 컴포넌트가 또 만들면 중복 연결, StrictMode에서 발화 두 번.

### After — useChatSocket

```tsx
const { status, liveMessages, log, send } = useChatSocket({ roomId, sessionId });
```

WsClient 인스턴스 한 개, Zod 검증, 재연결 5회, 로그 노출, dispose 자동.

## 장단점 (Trade-offs)

- **채택안: 클래스 + emit-subscribe + React hook 래퍼**
  - 장점: 라이프사이클 단일 소유, 다중 구독 가능, dispose로 메모리 정리.
  - 단점: 코드량 증가, 클래스 vs 함수형 취향 호불호.
- **미채택안: WebSocket을 모듈 스코프 singleton**
  - 장점: 더 단순.
  - 단점: roomId 변경/재연결 시 lifecycle 관리 복잡, SSR에서 모듈 로드만으로 연결 시도하는 사고.
- **미채택안: socket.io-client / reconnecting-websocket 같은 라이브러리**
  - 장점: 재연결 등 기본 제공.
  - 단점: 백엔드가 native WebSocket이라 호환성 문제, 학습 가치 더 큰 native 직접 구현 선택.

## 영향 (Impact)

- 재연결 5회 폴리시로 백엔드 깜빡 재시작에 자동 복구.
- parse-error는 채팅에 영향 없이 inspector에서만 확인 → UX 안정.
- 다음 커밋(채팅 UI)에서는 `useChatSocket`만 호출하면 라이프사이클 무관 작성.

## 더 읽을거리 (Refs)

- MDN WebSocket: https://developer.mozilla.org/docs/Web/API/WebSocket
- close codes: https://developer.mozilla.org/docs/Web/API/CloseEvent/code
- 관련 노트: [[03-strictmode-double-connect]], [[01-zod-patterns]]
