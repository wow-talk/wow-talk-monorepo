# docs

wow-talk front-end 학습 자료 인덱스. 카테고리는 헌법(`../CLAUDE.md` §11) 정의대로.

## 카테고리

| 폴더 | 주제 |
|---|---|
| `00-project-setup/` | 의존성 결정, 폴더 구조, env, pnpm |
| `10-nextjs/` | App Router, RSC vs Client, 폰트/메타데이터 |
| `20-react/` | hooks, Context vs Zustand, useReducer 비교 |
| `30-typescript/` | strict mode, Zod 패턴, narrowing |
| `40-websocket/` | lifecycle, 재연결 전략, StrictMode 더블 마운트 |
| `50-styling/` | vanilla-extract, 디자인 토큰 매핑 |
| `60-state-data/` | TanStack Query, Zustand, 서버 상태 vs 클라이언트 상태 |
| `70-architecture/` | 슬래시 커맨드 흐름 등 단면 설계 |
| `90-decisions/` | ADR (영속적 결정 기록) |

## 작성 규칙

- 카테고리 폴더 명: `NN-kebab` 두 자리 (간격 10, 끼워넣기 여유)
- 카테고리 내 파일: `NN-kebab.md` 두 자리
- ADR: `NNNN-noun-kebab.md` 네 자리
- 템플릿: `_template/learning-note.md`, `_template/adr.md`

## 노트 인덱스

### 00-project-setup
- [00. 의존성 결정 (2026-05-20)](00-project-setup/00-dependency-decisions.md) — vanilla-extract / TanStack Query / Zustand / Zod / nanoid 5종 선택 근거
