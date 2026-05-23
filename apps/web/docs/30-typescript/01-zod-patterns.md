# 01. Zod 런타임 검증 패턴 (2026-05-20)

> 외부와의 모든 데이터 접점(REST 응답, env, WS 메시지)을 Zod 스키마로 parse해서 "타입은 컴파일 시점 약속, 런타임 데이터는 별개"라는 함정을 닫는다.

## 어디서 (Where)

- `src/types/api.ts` — REST 응답 스키마 (Channel, ChatMessageResult)
- `src/lib/api/client.ts` — apiFetch가 응답을 parse
- `src/lib/env.ts` — 환경 변수 parse
- `src/lib/ws/schemas.ts` — WS 메시지 parse (다음 커밋)

## 무엇을 (What)

### 1) 스키마 + 타입을 한 파일에

```ts
export const ChannelSchema = z.object({
  roomId: z.string().min(1),
  transportMode: z.enum(["WEBSOCKET", "RAW_TCP"]),
});
export type Channel = z.infer<typeof ChannelSchema>;
```

`z.infer<typeof Schema>`로 타입을 스키마에서 추론. 스키마와 타입이 같은 파일에 있어 동기가 깨질 일 없음(헌법의 "별도 \*.types.ts 금지" 조항 근거).

### 2) parse vs safeParse

```ts
// parse — 실패 시 throw
const data = Schema.parse(json);

// safeParse — discriminated union 결과로 반환
const result = Schema.safeParse(json);
if (!result.success) {
  console.error(result.error.issues);
  return;
}
const data = result.data;
```

`apiFetch`/`env.ts`는 throw 동선이 자연스러워 `safeParse` 후 직접 throw. WS 메시지는 한 메시지 실패가 전체 연결을 끊으면 안 되므로 `safeParse` + 에러 로깅으로 처리.

### 3) discriminatedUnion (WS 메시지에서 사용 예정)

```ts
const InboundSchema = z.discriminatedUnion("type", [
  ConnectedSchema, ChatMessageSchema, ErrorSchema,
]);
```

`type` 필드 값에 따라 정확한 sub-스키마를 선택. 타입 추론도 `type === "CHAT_MESSAGE"`로 좁히면 자동으로 ChatMessage 타입.

### 4) apiFetch 패턴

```ts
export async function apiFetch<T>(path: string, schema: z.ZodType<T>, init?: RequestInit): Promise<T> {
  const res = await fetch(`${env.NEXT_PUBLIC_API_BASE}${path}`, init);
  if (!res.ok) throw new ApiError(res.status, ...);
  const json = await res.json();
  const parsed = schema.safeParse(json);
  if (!parsed.success) throw new ApiSchemaError(path, parsed.error.issues);
  return parsed.data;
}
```

핵심:

- 스키마를 인자로 받음 → 모든 호출 측이 응답 검증을 강제당함(잊을 수 없음).
- HTTP 에러와 schema 에러를 다른 Error 타입으로 분리(`ApiError` vs `ApiSchemaError`) → 호출 측에서 분기 가능.

## 왜 (Why)

### 1) TypeScript만으로 부족한 이유

타입은 컴파일 시점 약속이다. 런타임에는 `as Channel` 캐스팅이 거짓말일 수 있음:

```ts
const data = await res.json() as Channel; // 거짓말 가능
```

백엔드가 필드를 빼먹거나, 새 enum 값을 추가하거나, blank 값을 보내도 컴파일러는 모른다. Zod parse는 이 거짓말을 즉시 들춘다.

### 2) 백엔드 record blank 검증과 동일 강도

백엔드 `RoomId.value`는 blank를 IllegalArgumentException으로 거부한다. 프론트도 `z.string().min(1)`로 같은 강도 검증 → 잘못된 입력이 네트워크에 도달하기 전 차단.

### 3) 계약 변경 즉시 감지

백엔드가 응답 필드 이름을 바꾸거나 새 type을 추가하면 Zod parse가 실패 → 어디서 깨졌는지 stack trace로 즉시 추적. 그렇지 않으면 화면 어느 구석에서 `undefined`가 흘러다니다 사고 발생.

### 4) 같은 스키마를 여러 contexts에서 재사용

`ChatMessageResultSchema`는 REST 히스토리에서 검증 + 추후 모노레포 시 packages/shared로 이전해 백엔드와 공유 가능.

## Before / After

### Before — 캐스팅 의존

```ts
const res = await fetch("/api/v1/channels/lobby");
const data = (await res.json()) as Channel;
return data; // 백엔드가 필드를 빼먹어도 모름
```

### After — Zod parse

```ts
const data = await apiFetch("/api/v1/channels/lobby", ChannelSchema);
// data는 검증된 Channel. 위반 시 ApiSchemaError로 throw.
```

## 장단점 (Trade-offs)

- **채택안: Zod safeParse + custom Error 분리**
  - 장점: 런타임 안전, 명확한 에러 분류.
  - 단점: 스키마와 타입 둘 다 작성(타입 추론으로 50%만 작성).
- **미채택안: 단순 캐스팅 (`as Channel`)**
  - 장점: 셋업 0.
  - 단점: 위 모든 위험.
- **미채택안: io-ts / valibot / arktype**
  - 장점: 일부는 더 가벼움(valibot).
  - 단점: 생태계/문서가 Zod에 한참 못 미침.

## 영향 (Impact)

- 백엔드 계약 변경 silent 깨짐 차단.
- 런타임 부담: parse 한 번이라 무시 가능 수준.
- 학습 곡선: `z.object`, `z.array`, `z.enum`, `z.literal`, `z.discriminatedUnion`, `safeParse` 정도면 본 프로젝트 전체 커버.

## 더 읽을거리 (Refs)

- Zod 공식: https://zod.dev
- v4 변경점: https://github.com/colinhacks/zod/blob/main/packages/zod/CHANGELOG.md
- 관련 노트: [[00-tanstack-query-basics]], [[02-env-and-pnpm]]
