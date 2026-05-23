# Team Workflow

## 목적

세 명이 같은 모노레포에서 작업할 때 `main`을 안정적으로 유지하기 위한 최소 규칙이다.

팀 구성:

- Backend: backend, API, realtime protocol
- Frontend: `apps/web`
- DevOps: infra, deployment, AWS, container

## 기본 원칙

- `main`은 항상 실행 가능한 기준 브랜치로 유지한다.
- `main`에 직접 push하지 않는다.
- 작업은 브랜치에서 진행하고 PR로 합친다.
- 자기 영역만 바꾸는 PR은 가볍게 확인하고 merge한다.
- 공용 계약을 바꾸는 PR은 영향받는 사람이 확인한다.

## 브랜치 이름

```txt
be/<work-name>
fe/<work-name>
infra/<work-name>
doc/<work-name>
```

예시:

```txt
be/protocol-envelope
be/room-member
fe/chat-ui-sync
infra/ecs-deploy
doc/team-workflow
```

## 작업 흐름

```txt
1. main 최신화
2. 작업 브랜치 생성
3. 기능별 커밋
4. 로컬 빌드/테스트
5. 원격 브랜치 push
6. PR 생성
7. 영향받는 사람이 확인
8. main merge
```

## 리뷰 기준

```txt
백엔드 내부만 변경 -> 백엔드 담당자가 확인 후 merge 가능
프론트 내부만 변경 -> 프론트 담당자가 확인 후 merge 가능
인프라 내부만 변경 -> DevOps 담당자가 확인 후 merge 가능
공용 계약 변경 -> 영향받는 담당자 확인 후 merge
```

공용 계약 예시:

- `docs/frontend-backend-contract.md`
- `docs/websocket-api.md`
- `docs/realtime-protocol-v1.md`
- REST request/response schema
- WebSocket message format
- `.env.example`
- `compose.yaml`
- Dockerfile
- Terraform
- root `package.json`
- Gradle settings

## 권장 GitHub 설정

- `main` 직접 push 금지
- PR 필수
- force push 금지
- CI가 생기면 backend build와 frontend build 통과 필수

## 커밋 메시지

커밋 메시지는 한국어로 작성한다.

예시:

```txt
feat: 게스트 사용자 발급 추가
feat: 웹소켓 연결 식별자 분리
docs: 연결과 방 참여 계약 갱신
test: 방 참여 서비스 테스트 추가
chore: 로컬 개발 스크립트 정리
```
