# 00. 슬래시 커맨드 흐름 (2026-05-20)

> ChatComposer 입력이 어떻게 파싱되고, 어디서 분기되고, 결과가 어떻게 inspector에 흘러가는지 전체 데이터 흐름. 한 군데에서 봐두면 새 커맨드 추가나 디버깅이 직관적.

## 어디서 (Where)

- `src/lib/commands/parser.ts` — 입력 한 줄 → ParsedInput
- `src/lib/commands/registry.ts` — token → CommandDefinition lookup
- `src/lib/commands/handlers/*.ts` — 각 커맨드 부수효과
- `src/lib/inspector/bus.ts` — emit-subscribe 글로벌 버스
- `src/stores/inspectorStore.ts` — bus를 sink로 받는 Zustand store
- `src/features/chat/ChatPanel.tsx` — 통합 진입점

## 전체 흐름

```
[사용자 입력]
   │ "/inspect" 또는 "안녕"
   ▼
[ChatComposer]
   │ form submit → onSubmit(raw)
   ▼
[ChatPanel.handleSubmit]
   │ parseInput(raw)
   ▼
[ParsedInput 분기]
   ├─ kind: "empty"   → 무시
   ├─ kind: "text"    → chat.sendMessage(value) → WsClient.send({ type:"SEND_MESSAGE" })
   └─ kind: "command" → lookupCommand(token)
                         ├─ null → publishInspectorLine({ kind:"system", text:"[unknown command] /xxx" })
                         └─ CommandDefinition → cmd.handler(ctx, args)
                                                  └─ 핸들러가 부수효과 + publishInspectorLine

[publishInspectorLine]
   │
   ▼
[inspectorBus listeners]
   │ for-of dispatch
   ▼
[inspectorStore.push]
   │ FIFO 500줄 buffer
   ▼
[InspectorPanel] (커밋 9에서 추가)
   │ store.lines 구독 → 라인 렌더
   ▼
[화면]
```

WsClient의 lifecycle 이벤트(`status`, `message`, `retry`, `parse-error`)도 같은 inspectorBus로 fan-out. 즉 inspector는 슬래시 커맨드 출력 + WebSocket lifecycle을 한 채널에서 본다.

## 핵심 규약

### 1) 출력은 inspectorBus 전용

핸들러나 parser가 채팅으로 무언가 송출하지 않는다. 출력이 필요하면 `publishInspectorLine`만.

### 2) 등록되지 않은 슬래시는 inspector에만

```ts
if (!cmd) {
  publishInspectorLine({ kind: "system", text: `[unknown command] ${parsed.token}` });
  return;
}
```

`/`로 시작하는 어떤 입력도 채팅에 송신되지 않는다(헌법 13번 14번 항목). v0에서 일반 메시지로 `/path/to/file` 같은 텍스트를 보내야 한다면 추후 escape 규약(예: 첫 글자 `\/`)을 별도 결정한다.

### 3) 핸들러는 부수효과만, 반환값 없음

```ts
export type CommandHandler = (ctx: CommandContext, args: string[]) => void;
```

성공/실패 같은 결과를 호출 측에 반환하지 않는다. 모두 inspector 라인으로 보고.

### 4) inspectorStore는 bus의 sink

핸들러/wsClient/parser 어디서도 `useInspectorStore.getState().push(...)` 같은 직접 접근을 하지 않는다(예외: `clear`, `toggle`처럼 store가 데이터의 진실인 작업). 라인 추가는 항상 bus를 통해.

이유: 라인 출처가 다양해도(WsClient, command handler, future inspector self-log) 한 채널로 모이면 디버깅이 직선적. 또 추후 inspector를 두 개 띄우거나 라인에 필터링을 걸 때 listener를 한 군데에서 갈아끼우면 됨.

### 5) inspectorStore의 직접 메서드는 store 자신의 상태 조작에만

- `toggle()` / `show()` / `hide()` — open 여부
- `clear()` — lines 비우기
- `push()` — 내부 sink 용도(외부 직접 호출 회피)

`/clear`처럼 lines를 비우는 건 inspectorStore.clear()를 직접 호출. 라인을 새로 만드는 게 아니므로 bus 우회가 자연스러움.

## 새 커맨드 추가 절차

1. `src/lib/commands/handlers/<name>.ts` — `CommandHandler` 구현
2. `src/lib/commands/registry.ts` — `commands` 배열에 추가
3. 학습 노트(필요 시) `docs/70-architecture/` 또는 ADR
4. ChatPanel은 손대지 않음 — registry만 변경

## 안티패턴

- ❌ 핸들러가 `chat.sendMessage` 호출 → 커맨드 결과가 채팅으로 송출됨
- ❌ ChatComposer가 직접 핸들러 호출 → ChatPanel을 우회하면 CommandContext의 disconnect 등 의존성 주입이 안 됨
- ❌ inspector 라인 출력 시 React state 직접 set → 다중 출처 동기화 깨짐. 항상 bus.

## 더 읽을거리 (Refs)

- 관련 노트: [[01-context-vs-zustand]], [[00-ws-lifecycle]]
- ADR: [[0004-slash-command-trigger]]
