# 01. 디자인 토큰 매핑표 (2026-05-20)

> DESIGN.md의 placeholder(`{colors.canvas}`)와 `vars.*`의 1:1 매핑표. 컴포넌트 스타일 작성 시 이 표를 참조해 토큰 우회 없이 사용.

## 어디서 (Where)

- DESIGN.md (디자인 시스템 원본)
- src/styles/tokens.ts (raw values)
- src/styles/theme.css.ts (`vars` export)

## 무엇을 (What)

DESIGN.md의 모든 토큰을 vanilla-extract `vars`로 이식. 카테고리별 매핑표는 아래.

## 매핑표

### Colors — Brand & Accent

| DESIGN.md | vars 경로 | 값 |
|---|---|---|
| `{colors.primary}` | `vars.color.primary` | `#cc785c` |
| `{colors.primary-active}` | `vars.color.primaryActive` | `#a9583e` |
| `{colors.primary-disabled}` | `vars.color.primaryDisabled` | `#e6dfd8` |
| `{colors.accent-teal}` | `vars.color.accentTeal` | `#5db8a6` |
| `{colors.accent-amber}` | `vars.color.accentAmber` | `#e8a55a` |

### Colors — Surface

| DESIGN.md | vars 경로 | 값 |
|---|---|---|
| `{colors.canvas}` | `vars.color.canvas` | `#faf9f5` |
| `{colors.surface-soft}` | `vars.color.surfaceSoft` | `#f5f0e8` |
| `{colors.surface-card}` | `vars.color.surfaceCard` | `#efe9de` |
| `{colors.surface-cream-strong}` | `vars.color.surfaceCreamStrong` | `#e8e0d2` |
| `{colors.surface-dark}` | `vars.color.surfaceDark` | `#181715` |
| `{colors.surface-dark-elevated}` | `vars.color.surfaceDarkElevated` | `#252320` |
| `{colors.surface-dark-soft}` | `vars.color.surfaceDarkSoft` | `#1f1e1b` |
| `{colors.hairline}` | `vars.color.hairline` | `#e6dfd8` |
| `{colors.hairline-soft}` | `vars.color.hairlineSoft` | `#ebe6df` |

### Colors — Text

| DESIGN.md | vars 경로 | 값 |
|---|---|---|
| `{colors.ink}` | `vars.color.ink` | `#141413` |
| `{colors.body-strong}` | `vars.color.bodyStrong` | `#252523` |
| `{colors.body}` | `vars.color.body` | `#3d3d3a` |
| `{colors.muted}` | `vars.color.muted` | `#6c6a64` |
| `{colors.muted-soft}` | `vars.color.mutedSoft` | `#8e8b82` |
| `{colors.on-primary}` | `vars.color.onPrimary` | `#ffffff` |
| `{colors.on-dark}` | `vars.color.onDark` | `#faf9f5` |
| `{colors.on-dark-soft}` | `vars.color.onDarkSoft` | `#a09d96` |

### Colors — Semantic

| DESIGN.md | vars 경로 | 값 |
|---|---|---|
| `{colors.success}` | `vars.color.success` | `#5db872` |
| `{colors.warning}` | `vars.color.warning` | `#d4a017` |
| `{colors.error}` | `vars.color.error` | `#c64545` |

### Typography — fontSize / lineHeight / letterSpacing (Composite)

DESIGN.md는 typography를 size+weight+line-height+letter-spacing 묶음으로 정의했지만, vanilla-extract는 atom 단위로 분리해 두고 컴포넌트 CSS에서 조합한다. 묶음 의도는 아래 표로 보존:

| DESIGN.md | fontSize | fontWeight | lineHeight | letterSpacing | fontFamily |
|---|---|---|---|---|---|
| `{typography.display-xl}` | `vars.fontSize.displayXl` (64) | regular | `displayXl` (1.05) | `displayXl` (-1.5px) | serif |
| `{typography.display-lg}` | `displayLg` (48) | regular | `displayLg` (1.1) | `displayLg` (-1px) | serif |
| `{typography.display-md}` | `displayMd` (36) | regular | `displayMd` (1.15) | `displayMd` (-0.5px) | serif |
| `{typography.display-sm}` | `displaySm` (28) | regular | `displaySm` (1.2) | `displaySm` (-0.3px) | serif |
| `{typography.title-lg}` | `titleLg` (22) | medium | `title` (1.3) | `normal` (0) | sans |
| `{typography.title-md}` | `titleMd` (18) | medium | `titleTight` (1.4) | `normal` | sans |
| `{typography.title-sm}` | `titleSm` (16) | medium | `titleTight` | `normal` | sans |
| `{typography.body-md}` | `bodyMd` (16) | regular | `body` (1.55) | `normal` | sans |
| `{typography.body-sm}` | `bodySm` (14) | regular | `body` | `normal` | sans |
| `{typography.caption}` | `caption` (13) | medium | `titleTight` | `normal` | sans |
| `{typography.caption-uppercase}` | `captionUppercase` (12) | medium | `titleTight` | `uppercase` (1.5px) | sans |
| `{typography.code}` | `code` (14) | regular | `code` (1.6) | `normal` | mono |
| `{typography.button}` | `button` (14) | medium | `button` (1.0) | `normal` | sans |
| `{typography.nav-link}` | `navLink` (14) | medium | `titleTight` | `normal` | sans |

### Font Family

| DESIGN.md 의도 | vars | 실제 대체 |
|---|---|---|
| Copernicus serif | `vars.font.serif` | Cormorant Garamond (next/font) → fallback Garamond / Times |
| StyreneB sans | `vars.font.sans` | Inter (next/font) → fallback -apple-system / Segoe UI |
| JetBrains Mono | `vars.font.mono` | JetBrains Mono (next/font) → fallback Courier New |

대체 결정 근거는 [[02-fonts-and-metadata]] 참고.

### Spacing

| DESIGN.md | vars 경로 | 값 |
|---|---|---|
| `{spacing.xxs}` | `vars.space.xxs` | `4px` |
| `{spacing.xs}` | `vars.space.xs` | `8px` |
| `{spacing.sm}` | `vars.space.sm` | `12px` |
| `{spacing.md}` | `vars.space.md` | `16px` |
| `{spacing.lg}` | `vars.space.lg` | `24px` |
| `{spacing.xl}` | `vars.space.xl` | `32px` |
| `{spacing.xxl}` | `vars.space.xxl` | `48px` |
| `{spacing.section}` | `vars.space.section` | `96px` |

### Radius

| DESIGN.md | vars 경로 | 값 |
|---|---|---|
| `{rounded.xs}` | `vars.radius.xs` | `4px` |
| `{rounded.sm}` | `vars.radius.sm` | `6px` |
| `{rounded.md}` | `vars.radius.md` | `8px` |
| `{rounded.lg}` | `vars.radius.lg` | `12px` |
| `{rounded.xl}` | `vars.radius.xl` | `16px` |
| `{rounded.pill}` | `vars.radius.pill` | `9999px` |
| `{rounded.full}` | `vars.radius.full` | `9999px` |

### Elevation

| DESIGN.md | vars 경로 | 값 |
|---|---|---|
| 1px hairline border (`{colors.hairline}`) | `vars.color.hairline` | 1px border용 색 |
| Soft drop shadow (rare) | `vars.shadow.soft` | `0 1px 3px rgba(20,20,19,0.08)` |

## 왜 (Why)

원본 DESIGN.md는 placeholder만 있고 실제 코드에 박힌 적 없음. 컴포넌트 작성 직전에 표 한 장으로 옮겨두면:

1. 디자이너 의도(`{spacing.section}` = 96px = 섹션 간격)가 코드 토큰명으로 살아남음.
2. `vars.space.section`을 칠 때 IDE 자동완성으로 96px이 거기에 있다는 사실을 잊지 않음.
3. 새 토큰이 필요해질 때 "DESIGN.md에 있는가"를 표로 빠르게 검색.

## 장단점 (Trade-offs)

- **채택안: typography를 atom 단위(fontSize / fontWeight / lineHeight / letterSpacing 별도)**
  - 장점: 조합 자유도, 토큰 트리가 평평해 IDE 탐색 직관적, `vars.fontSize.titleMd` 같이 명료.
  - 단점: 컴포넌트 CSS에서 4개 줄 반복(`display-xl`을 매번 4줄로). 다음 단계에서 `styleVariants`나 helper로 묶을 예정.
- **미채택안: composite 객체(`vars.typography.displayXl = { fontSize, weight, ... }`)**
  - 장점: 컴포넌트 한 줄로 적용.
  - 단점: vanilla-extract `createGlobalTheme`는 leaf가 string이라 nested style 객체를 CSS 변수로 못 펼침. 별도 헬퍼/recipe로 우회해야 함. 학습 비용 커서 다음 단계로.

## 영향 (Impact)

- 디자인 변경 시 `tokens.ts` 한 파일만 수정 → 전체 UI 일관 반영.
- 컴포넌트 작성 시 hex/px 리터럴 작성 금지(헌법 7번 조항) 강제 가능.
- 이 매핑표가 신뢰 가능한 한, DESIGN.md를 따로 열어보지 않아도 코드만으로 디자인 의도 추론 가능.

## 더 읽을거리 (Refs)

- DESIGN.md (front-end 루트)
- 관련 노트: [[00-vanilla-extract-intro]], [[02-fonts-and-metadata]]
