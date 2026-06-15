# 02. CSS-in-TS 조건부 클래스 패턴 (2026-05-20)

> vanilla-extract는 빌드 타임 정적 CSS라 "조건에 따라 다른 스타일"을 어떻게 표현할지가 학습 포인트. `styleVariants` 패턴이 핵심.

## 어디서 (Where)

- `src/features/inspector/InspectorPanel.css.ts` — open/closed 분기
- `src/app/rooms/[roomId]/page.css.ts` — shell open/closed grid 분기
- `src/features/chat/MessageItem.css.ts` — mine/other 버블 분기
- `src/features/chat/SessionBadge.css.ts` — socket status별 dot 색

## 무엇을 (What)

### 1) 동적 스타일을 vanilla-extract로 표현하는 방법

vanilla-extract는 빌드 타임에 클래스를 추출하므로 "런타임에 inline style을 동적으로 만든다"는 패턴은 안 됨. 대신:

| 패턴 | 사용 시점 |
|---|---|
| `style({...})` | 단일 클래스 |
| `styleVariants({ a, b, c }, ...)` | 유한 개의 사전 정의된 분기 |
| `recipe({...})` | 여러 boolean variant 조합 (필요 시 도입) |
| `assignVars(vars, {...})` | CSS 변수 런타임 갱신(드물게) |

### 2) styleVariants 사용 예 — InspectorPanel

```ts
import { style, styleVariants } from "@vanilla-extract/css";
import { vars } from "@/styles/theme.css";

const panelBase = style({
  display: "flex",
  flexDirection: "column",
  backgroundColor: vars.color.surfaceDark,
  transition: "transform 220ms ease, opacity 220ms ease",
});

export const panel = styleVariants({
  open: [
    panelBase,
    { transform: "translateX(0)", opacity: 1 },
  ],
  closed: [
    panelBase,
    { transform: "translateX(100%)", opacity: 0, pointerEvents: "none" },
  ],
});
```

사용:

```tsx
<aside className={open ? styles.panel.open : styles.panel.closed} />
```

`styleVariants`는 키별로 별도 클래스를 만들고 base 클래스를 자동으로 합친다. 즉 `panel.open` 클래스 하나가 `panelBase` + open 전용 규칙을 모두 포함.

### 3) recipe (이번 커밋엔 미사용, 참고)

여러 boolean 조합이 필요할 때:

```ts
import { recipe } from "@vanilla-extract/recipes";

export const button = recipe({
  base: { /* ... */ },
  variants: {
    intent: { primary: {/*...*/}, secondary: {/*...*/} },
    size: { sm: {/*...*/}, md: {/*...*/}, lg: {/*...*/} },
    disabled: { true: {/*...*/} },
  },
  defaultVariants: { intent: "primary", size: "md" },
});

// 사용
<button className={styles.button({ intent: "primary", size: "sm" })} />
```

본 프로젝트는 아직 단순 boolean 분기만 있어 `styleVariants`로 충분. 추후 버튼 패밀리가 늘어나면 `@vanilla-extract/recipes` 추가 도입 고려.

### 4) clsx와 vanilla-extract 조합

여러 클래스를 동시에 적용하고 싶을 때:

```tsx
import clsx from "clsx";
<div className={clsx(styles.base, isMine && styles.mine, isHighlight && styles.highlight)} />
```

본 프로젝트는 아직 단순 분기라 `clsx` 미도입. 도입 시 추가 의존성 1개라 부담 적음.

### 5) 트랜지션을 styleVariants와 조합

CSS transition은 base에 두고 variant가 transition 대상 속성을 변경:

```ts
const panelBase = style({
  transition: "transform 220ms ease, opacity 220ms ease",
});

export const panel = styleVariants({
  open:   [panelBase, { transform: "translateX(0)",    opacity: 1 }],
  closed: [panelBase, { transform: "translateX(100%)", opacity: 0 }],
});
```

variant 전환 시 base의 transition이 살아있어 자연스러운 슬라이드 인/아웃.

## 왜 (Why)

### 1) 왜 inline style이나 className 문자열 조립이 안 좋은가

```tsx
// 안티패턴 1: inline style
<div style={{ transform: open ? "translateX(0)" : "translateX(100%)" }} />
```

- 토큰 자동완성 없음 (hex 직접 작성으로 미끄러짐)
- 의사 클래스(`:hover`), 미디어 쿼리 표현 못 함

```tsx
// 안티패턴 2: 문자열 조립
<div className={`panel ${open ? "panel--open" : ""}`} />
```

- 오타 컴파일러가 못 잡음
- vanilla-extract 클래스 해시(`panel__abc123`)와 호환 안 됨

### 2) 왜 styleVariants가 좋은가

- 키별 클래스가 컴파일 타임에 다 만들어짐 → 런타임 비용 0.
- TS 타입으로 `styles.panel.open` 자동완성.
- variant 추가 시 한 군데(`styleVariants` 객체)에 키 추가.

### 3) 미디어 쿼리는 selectors나 `@media`로

vanilla-extract는 nested 미디어 쿼리를 지원:

```ts
const panel = style({
  display: "flex",
  "@media": {
    "(max-width: 768px)": {
      position: "fixed",
      inset: 0,
    },
  },
});
```

InspectorPanel은 데스크탑에서 grid column 안에 자리잡지만, 모바일(768px 이하)에서는 fixed로 풀스크린 시트가 되도록 이 패턴 사용.

## Before / After

### Before — 잘못 동적

```tsx
<aside style={{ transform: open ? "translateX(0)" : "translateX(100%)" }}>
```

문제: transition 없음, 토큰 우회.

### After — styleVariants + base transition

```ts
const panelBase = style({ transition: "transform 220ms ease, opacity 220ms ease" });
export const panel = styleVariants({
  open:   [panelBase, { transform: "translateX(0)",    opacity: 1 }],
  closed: [panelBase, { transform: "translateX(100%)", opacity: 0 }],
});
```

```tsx
<aside className={open ? styles.panel.open : styles.panel.closed} />
```

부드러운 슬라이드 + 토큰 안전.

## 장단점 (Trade-offs)

- **채택안: styleVariants + 분기 className**
  - 장점: 빌드 타임 추출, 타입 안전, 토큰 우회 0.
  - 단점: variant 키가 늘면 객체가 길어짐(그러면 recipe로 이동).
- **미채택안: inline style**
  - 장점: 가장 단순.
  - 단점: 토큰 우회, hover/미디어 표현 못 함.
- **미채택안: recipe**
  - 장점: variant 조합이 복잡할 때 깔끔.
  - 단점: 별도 패키지(`@vanilla-extract/recipes`). 본 단계에선 과함.

## 영향 (Impact)

- 슬라이드 인/아웃이 토큰 안에서 일관 처리.
- 모바일 시트가 미디어 쿼리 한 줄로 해결.
- variant 추가가 코드 1줄.

## 더 읽을거리 (Refs)

- vanilla-extract styleVariants: https://vanilla-extract.style/documentation/styling/#stylevariants
- recipes: https://vanilla-extract.style/documentation/packages/recipes/
- 관련 노트: [[00-vanilla-extract-intro]], [[01-design-tokens-mapping]]
