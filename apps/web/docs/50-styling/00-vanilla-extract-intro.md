# 00. vanilla-extract 인트로 (2026-05-20)

> 디자인 토큰을 TS로 정의하고 빌드 타임에 CSS로 추출하는 CSS-in-TS 라이브러리. 런타임 비용 0, IDE 자동완성 100%.

## 어디서 (Where)

- `src/styles/tokens.ts` — 토큰 raw values (SSOT)
- `src/styles/theme.css.ts` — `createGlobalTheme`로 토큰을 CSS 변수로 노출
- `src/styles/globals.css.ts` — `globalStyle`로 reset + body 기본
- `src/app/layout.tsx` — `import "@/styles/globals.css";` 한 줄로 글로벌 등록

## 무엇을 (What)

vanilla-extract는 `*.css.ts` 파일을 작성하면 webpack(또는 vite/esbuild) 로더가 빌드 타임에 그 파일을 평가해 **실제 CSS 파일을 생성하고 JS 쪽에는 클래스명/CSS 변수 참조만 남긴다**. 런타임에는 일반 CSS 동작.

도입한 API 3종:

| API | 역할 |
|---|---|
| `createGlobalTheme(selector, tokens)` | 토큰 트리를 CSS 변수로 펼침 + 동일 구조의 TS 객체 반환 |
| `globalStyle(selector, rules)` | reset/element 기본값 같은 글로벌 CSS |
| `style(rules)` | 컴포넌트 스코프 클래스(다음 커밋부터 사용) |

추가로 `styleVariants`, `recipe`, `sprinkles` 같은 고급 API가 있지만 이번 커밋에서는 안 씀.

## 왜 (Why)

DESIGN.md는 placeholder 표기(`{colors.canvas}`)로 토큰 카탈로그를 정의한다. 이걸 어디에 어떻게 보관할지가 첫 결정:

| 옵션 | 토큰 표기 | TS 자동완성 | 런타임 비용 |
|---|---|---|---|
| CSS Module + CSS 변수 | `var(--color-canvas)` | 없음(문자열) | 0 |
| Tailwind v4 | `bg-canvas` | 있음(빌드 후) | 0 |
| styled-components | template string | 부분 | 있음(런타임 CSS 주입) |
| **vanilla-extract** | **`vars.color.canvas`** | **있음** | **0** |

학습 목표가 "디자인 토큰을 타입으로 다루는 경험"인 만큼 vanilla-extract가 가장 맞는 선택. 토큰 오타 시 IDE에서 즉시 빨간 줄 — DESIGN.md를 곧 코드 계약으로 만든다.

## Before / After

### Before — `src/app/globals.css` (보일러플레이트)

```css
:root {
  --background: #ffffff;
  --foreground: #171717;
}

body {
  color: var(--foreground);
  background: var(--background);
  font-family: Arial, Helvetica, sans-serif;
}
```

- 변수명이 문자열이라 오타 시 무음 실패.
- 폰트/색이 의미적 토큰이 아닌 임의 값.

### After — `src/styles/globals.css.ts`

```ts
import { globalStyle } from "@vanilla-extract/css";
import { vars } from "./theme.css";

globalStyle("body", {
  backgroundColor: vars.color.canvas,
  color: vars.color.ink,
  fontFamily: vars.font.sans,
  fontSize: vars.fontSize.bodyMd,
  lineHeight: vars.lineHeight.body,
});
```

- `vars.color.kanvas`라고 오타 내면 컴파일 에러.
- 모든 색/폰트가 의미적 이름으로 묶임.

### After — `src/styles/theme.css.ts`

```ts
import { createGlobalTheme } from "@vanilla-extract/css";
import { tokens } from "./tokens";

export const vars = createGlobalTheme(":root", tokens);
```

생성되는 실제 CSS(빌드 후):

```css
:root {
  --color-canvas__faf9f5: #faf9f5;
  --color-ink__141413: #141413;
  /* ... */
}
```

변수명은 vanilla-extract가 충돌 안전성을 위해 해시를 붙임. JS에서는 `vars.color.canvas`로 접근하면 위 `--color-canvas__faf9f5` 같은 실제 CSS 변수명을 반환.

## 장단점 (Trade-offs)

- **채택안: vanilla-extract**
  - 장점: 타입 안전, 런타임 0, 빌드 결과가 일반 CSS라 디버깅 직관적, 토큰 우회 자동 차단.
  - 단점: `*.css.ts` 확장자가 낯섦, `style()`로 만든 클래스를 동적 토글하려면 `clsx` 같은 헬퍼 필요, Turbopack 정식 지원 전이라 webpack 모드 강제.
- **미채택안: CSS Modules + CSS 변수**
  - 장점: Next 기본 지원, 학습 곡선 0.
  - 단점: 토큰 자동완성 없음, hex 직접 작성 막을 룰이 강제되지 않음.
- **미채택안: styled-components**
  - 장점: API 친숙.
  - 단점: 런타임 CSS-in-JS, App Router SSR 셋업 복잡, 번들 사이즈.

## 영향 (Impact)

- **런타임**: vanilla-extract 자체는 0. 결과물은 일반 CSS.
- **번들**: 컴포넌트 스타일이 별도 CSS 파일로 분리돼 JS 번들에서 빠짐.
- **개발 경험**: IDE에서 `vars.`만 쳐도 모든 토큰 자동완성. 디자인 변경 시 `tokens.ts` 한 파일만 수정.
- **빌드 시간**: webpack 단계 추가지만 dev에서 체감 불가.
- **학습 곡선**: `style()` vs `globalStyle()` 차이, scope, recipe 등 익혀야 할 개념이 있음. 다음 커밋에서 컴포넌트 스타일 작성할 때 추가 노트 예정.

## 더 읽을거리 (Refs)

- 공식: https://vanilla-extract.style/documentation/
- Next 통합: https://vanilla-extract.style/documentation/integrations/next/
- API 레퍼런스: https://vanilla-extract.style/documentation/api/global-style/
- 관련 노트: [[01-design-tokens-mapping]], [[00-dependency-decisions]]
