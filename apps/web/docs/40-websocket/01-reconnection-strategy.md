# 01. WebSocket 재연결 전략 (2026-05-20)

> 비정상 close 시 지수 백오프(500ms / 1s / 2s / 4s / 8s)로 최대 5회 재연결. inspector 도입 후 이 정책이 시각적으로 관찰 가능해졌다.

## 어디서 (Where)

- `src/lib/ws/wsClient.ts` — `BACKOFF_DELAYS_MS`, `onclose` 핸들러
- `src/lib/ws/useChatSocket.ts` — retry 이벤트를 inspector bus로 fan-out
- inspector 패널 — retry 라인 시각화

## 무엇을 (What)

### 정책 요약

| attempt | 대기 시간 | 누적 시간 |
|---|---|---|
| 1 | 500ms | 0.5s |
| 2 | 1000ms | 1.5s |
| 3 | 2000ms | 3.5s |
| 4 | 4000ms | 7.5s |
| 5 | 8000ms | 15.5s |
| 이후 | 시도 안 함 | — |

총 15.5초 안에 5번 시도 후 멈춤. 그 이후는 사용자가 새로고침/페이지 이동으로 새 연결 시작해야 함.

### 발화 조건

- `event.wasClean === false` (비정상 close)인 경우에만 재연결.
- 정상 close(`wasClean === true`) 또는 `/disconnect` 호출(reconnect flag false)에서는 시도 안 함.
- `client.dispose()` 또는 effect cleanup으로 client 자체가 사라지면 retry timer 정리.

### inspector에 나타나는 모습

```
12:34:56  STATUS    socket -> open
12:35:10  STATUS    socket -> error
12:35:10  STATUS    socket -> closed
12:35:10  RETRY     attempt 1 in 500ms (close 1006)
12:35:11  STATUS    socket -> connecting
12:35:11  STATUS    socket -> error
12:35:11  STATUS    socket -> closed
12:35:11  RETRY     attempt 2 in 1000ms (close 1006)
...
12:35:26  RETRY     attempt 5 in 8000ms (close 1006)
12:35:34  STATUS    socket -> error
12:35:34  STATUS    socket -> closed
(끝 — 더 이상 retry 없음)
```

## 왜 (Why)

### 지수 백오프인 이유

선형(1s 간격 5회)이나 짧은 고정 간격은 서버 다운/네트워크 끊김 같은 "잠시 후엔 살아날 가능성" 시나리오에 트래픽 폭주를 유발. 지수는:

- 첫 시도는 빠르게(0.5s) → 깜빡 끊김(예: 와이파이 재연결)에 빠른 복구
- 그래도 안 되면 점진적으로 늘려 백엔드 부담 감소
- 5회 후 멈춤 → 무한 재시도로 인한 자원 소모/오해 차단

### 5회로 끊은 이유

업계 표준에 정답은 없지만:
- 1-2회는 일시적 깜빡임만 커버. 서버 재시작(보통 5-15초)을 못 잡음
- 10회 이상은 거의 항상 서버가 죽은 케이스라 의미 없음
- 5회는 15초 안에 5번 시도 → 서버 hot reload 또는 짧은 다운타임 cover

학습 데모라 보수적으로 5회. 추후 ADR로 정식화하거나 환경 변수로 노출 가능.

### 정상 close에서는 시도 안 하는 이유

- 사용자가 페이지를 떠나면 브라우저가 close를 정상으로 마무리(`wasClean === true`).
- 이때 재연결하면 페이지 사라진 뒤 좀비 연결.
- `/disconnect` 커맨드는 사용자가 명시적으로 끊겠다는 의사 → reconnect flag false.

### closeSocket의 핸들러 null 처리

```ts
s.onopen = null;
s.onmessage = null;
s.onerror = null;
s.onclose = null;
s.close();
```

이걸 안 하면 close 도중 onclose가 두 번 발화해 retry 로직이 같은 시도 번호로 두 번 실행되는 사고가 일어난다. WsClient는 이 패턴을 라이브러리 수준에서 보장.

## Before / After

### Before — 재연결 없음 (간단 effect)

```ts
useEffect(() => {
  const ws = new WebSocket(url);
  return () => ws.close();
}, [url]);
```

서버 깜빡 재시작 → 페이지 새로고침 없이는 영영 끊김.

### After — WsClient + 5회 백오프

```ts
const { status, log } = useChatSocket({ roomId, sessionId });
// retry 이벤트는 inspector로 자동 흘러감
```

서버 깜빡(15초 이내)에서는 사용자 모르게 복구. 그 이상은 inspector에서 retry 5회 멈춘 흔적 확인 후 새로고침 안내.

## 장단점 (Trade-offs)

- **채택안: 지수 백오프 5회**
  - 장점: 깜빡 끊김 자동 복구, 트래픽 폭주 회피, 멈춤 시점이 예측 가능.
  - 단점: 백엔드가 15초 이상 다운이면 사용자가 직접 새로고침해야 함(자동 무한 시도 안 함).
- **미채택안: 무한 재시도**
  - 장점: 가장 단순.
  - 단점: 좀비 연결, 사용자가 "연결됨"으로 잘못 인지.
- **미채택안: 라이브러리(reconnecting-websocket)**
  - 장점: 코드량 0.
  - 단점: 학습 가치 손실, 추가 의존성.

## 영향 (Impact)

- 백엔드 hot reload/짧은 다운에서 사용자 체감 끊김 거의 없음.
- inspector 도입으로 정책이 시각화되어 "왜 멈췄나" 디버깅 직선적.
- 추후 정책 변경 시 `BACKOFF_DELAYS_MS` 한 곳만 수정.

## 더 읽을거리 (Refs)

- close codes: https://developer.mozilla.org/docs/Web/API/CloseEvent/code
- 관련 노트: [[00-ws-lifecycle]], [[03-strictmode-double-connect]]
- ADR 후보: 0005-ws-reconnection-policy (정식화 시점)
