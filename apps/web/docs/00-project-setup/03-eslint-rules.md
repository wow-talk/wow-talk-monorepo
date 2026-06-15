# 03. ESLint 룰 강화 (2026-05-20)

> 헌법(CLAUDE.md)의 룰을 코드 수준에서 강제. 시각 변화 없는 작업이라 커밋 컨벤션은 design이 아니라 style.

## 어디서 (Where)

- `eslint.config.mjs` — flat config 형식, 룰 4종 추가 + 라우팅 파일 예외 overrides

## 무엇을 (What)

추가된 룰 4가지:

| 룰 | 강도 | 의도 |
|---|---|---|
| `@typescript-eslint/no-explicit-any` | error | any 사용 금지(unknown + 좁히기) |
| `import/order` | error | 외부 → `@/*` → 같은 폴더 상대 순서 강제 |
| `import/no-default-export` | error | default export 금지 (라우팅 파일/설정 파일 예외) |
| `no-restricted-imports` (../../ 차단) | error | 두 단계 이상 상대경로 import 금지 |

라우팅/설정 파일 예외(`import/no-default-export`만):

```
src/app/**/{page,layout,error,not-found,loading,template,default,global-error}.tsx
next.config.ts
eslint.config.mjs
```

## 왜 (Why)

### 1) 헌법의 룰은 코드 수준에서 강제돼야 한다

문서에만 적혀있으면 사람이 잊는다. eslint가 잡아내면 자동 차단. 헌법 13번 항목에는 14가지 "하지 말 것"이 있는데 그중 자동화 가능한 것 4가지를 이번 커밋에서 룰로 옮김.

### 2) `any` 금지

런타임 검증을 Zod로 깔아둔 의미가 사라짐. `unknown`으로 받고 타입 가드 / Zod parse / 제네릭으로 좁히는 게 정답. eslint error로 막아두면 학습 도중 무심코 `any: any` 패턴으로 미끄러지는 사고 차단.

### 3) `import/order` (외부 → `@/*` → 상대)

큰 파일에서 import 영역만 봐도 "어떤 외부 의존성을 쓰는지 / 어떤 내부 모듈에 의존하는지" 즉시 파악. 사람마다 다른 순서로 import하면 review 시 diff에 잡음이 끼고 학습 누수가 생김.

설정 핵심:
- `groups`: `builtin → external → internal → (parent | sibling | index)` 순서
- `pathGroups`: `@/**`를 internal 그룹에 매핑
- `newlines-between: "always"`: 그룹 사이 빈 줄
- `alphabetize`: 같은 그룹 내 알파벳순

### 4) `import/no-default-export`

named export는 IDE refactor(rename) 안전성이 압도적. default export는 import 측에서 이름을 임의로 짓기 때문에 같은 컴포넌트가 다른 이름으로 import되는 사고가 흔함.

**라우팅 파일 예외**가 필요한 이유: Next.js 16의 App Router는 `page.tsx` / `layout.tsx` 등에서 default export를 요구. named export 병행도 거부함(다른 named export가 metadata/generateStaticParams처럼 Next 인식 항목이면 OK). 헌법 6번 보강 조항과 정확히 일치.

```js
{
  files: ["src/app/**/page.tsx", "src/app/**/layout.tsx", /* ... */, "eslint.config.mjs", "next.config.ts"],
  rules: { "import/no-default-export": "off" }
}
```

### 5) `no-restricted-imports`의 `../../*` 차단

```js
patterns: [{ group: ["../../*"], message: "두 단계 이상 ... @/* alias 사용" }]
```

`../`까지는 허용. `../../` 이상은 alias로 강제 → 폴더 이동(리팩터)에 강함, 가독성 ↑.

## Before / After

### Before — 룰 추가 전

```ts
// any가 silent 통과
function handle(data: any) { /* ... */ }

// import 순서 자유
import { vars } from "@/styles/theme.css";
import { useEffect } from "react";
import * as styles from "./X.css";

// default export 자유
export default function MyComp() { /* ... */ }

// ../../ 자유
import { foo } from "../../utils/foo";
```

### After — 룰 추가 후

```ts
// any error
function handle(data: any) {}
//                    ^^^ Error: Unexpected any.

// import/order로 정렬 강제
import { useEffect } from "react";        // external

import { vars } from "@/styles/theme.css"; // internal

import * as styles from "./X.css";         // sibling

// default export error (라우팅/설정 파일 예외)
export default function MyComp() {}
//             ^^^^^^^ Error: Prefer named exports.

// ../../ error
import { foo } from "../../utils/foo";
//                  ^^^^^^^^^^^^^^^^^ Error: 두 단계 이상의 상대경로 import 금지
```

## 이번 커밋에서 잡힌 baseline 위반

룰 추가 전, 기존 코드에 다음이 있었다:

- 13개 import/order 위반 (commands/handlers, ChatPanel, SessionBadge, registry, next.config.ts)
- 자동 fix(`--fix`)로 12개 해결
- 1개(`eslint.config.mjs`의 default export) 수동 처리(overrides에 추가)

추가로 룰 강화 이전 round에서 발견된 baseline 2건도 정리(같은 커밋):
- `react-hooks/set-state-in-effect` (React 19): `useChatSocket`이 effect 안에서 `setLiveMessages([])` 직접 호출 → React 19 권장 패턴(`render 중 props 변경 감지 후 즉시 setState`)으로 변경
- `react-hooks/exhaustive-deps` (warning): `ChatPanel.handleSubmit`이 `chat` 객체에 의존 → destructure 후 함수 reference 두 개만 deps에 명시

## 장단점 (Trade-offs)

- **채택안: 4개 룰 + overrides**
  - 장점: 헌법이 코드 수준에서 자동 강제, IDE에서 즉시 빨간 줄, refactor 안전성 증가.
  - 단점: 신규 합류자가 룰을 다 모르면 처음에 빨간 줄 폭주(README/CLAUDE.md를 따라 읽어야 함).
- **미채택안: 룰 없이 헌법만 문서로 두기**
  - 장점: 셋업 0.
  - 단점: 사람이 잊음, review에서만 잡힘.
- **미채택안: 더 강한 룰(예: `import/no-relative-parent-imports`)**
  - 장점: 모든 상대경로 차단.
  - 단점: `./` 같은 같은 폴더 import까지 막아 vanilla-extract `*.css.ts` import에 부담.

## 영향 (Impact)

- 빌드 단계에서 lint 통과 강제(`pnpm lint` CI에 넣으면 PR 단계 차단).
- 신규 코드 작성 시 IDE가 실시간으로 룰 위반 안내.
- 헌법 13번 항목 중 자동화 가능한 4개가 코드 수준으로 이전 → 헌법은 사람 의지 의존도가 줄어듦.

## 더 읽을거리 (Refs)

- ESLint flat config: https://eslint.org/docs/latest/use/configure/configuration-files
- eslint-plugin-import: https://github.com/import-js/eslint-plugin-import
- typescript-eslint no-explicit-any: https://typescript-eslint.io/rules/no-explicit-any
- React 19 set-state-in-effect: https://react.dev/reference/react/useState#storing-information-from-previous-renders
- 관련 노트: [[00-dependency-decisions]], CLAUDE.md 6번/13번 항목
