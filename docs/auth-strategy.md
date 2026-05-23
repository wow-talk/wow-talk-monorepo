# Auth Strategy

## 목표

초기 진입 장벽은 낮게 유지하면서, 나중에 소셜 로그인과 계정 연결을 붙일 수 있는 인증 구조를 만든다.

채팅과 게임은 입장이 빨라야 한다. 처음부터 로그인을 강제하면 사용자가 방에 들어오기 전에 이탈할 수 있다.

## 기본 전략

```txt
Phase 1: Guest Auth
Phase 2: Social Login
Phase 3: Guest -> Social Account Linking
Phase 4: Room Permission / Moderation
```

## Phase 1. Guest Auth

게스트는 회원가입 없이 방에 들어올 수 있다.

흐름:

```txt
사용자 닉네임 입력
-> guest user 생성
-> room 입장
-> connection session 생성
-> WebSocket 연결
```

초기 구현에서는 프론트가 임시 sessionId를 만드는 대신, 백엔드가 guest user와 connectionId를 발급하는 방향으로 간다.

예상 API:

```http
POST /api/v1/guests
Content-Type: application/json

{
  "displayName": "guest"
}
```

응답:

```json
{
  "userId": "guest-1",
  "displayName": "guest",
  "userType": "GUEST"
}
```

## Phase 2. Social Login

소셜 로그인은 추천한다. 채팅/게임 서비스는 가입 절차가 짧을수록 좋다.

우선순위 후보:

```txt
1. Google
2. Kakao
3. Apple
4. GitHub
```

한국 사용자 중심이면 Kakao를 고려한다. 초기 개발자/테스터 중심이면 GitHub도 편하다.

AWS 배포를 전제로 한 후보:

```txt
Amazon Cognito User Pool
Spring Security OAuth2 Resource Server
```

하지만 Cognito를 도메인에 직접 박지 않는다.

## 비용 원칙

Cognito는 Lite/Essentials 기준 무료 MAU 구간이 있어 초기에는 비용 부담이 낮을 수 있다. 그래도 비용은 설계 조건이다.

따라서:

- MVP는 guest auth로 시작한다.
- Cognito는 provider 중 하나로 취급한다.
- SMS MFA는 초기 기본값에서 제외한다.
- Plus tier 고급 보안 기능은 필요가 생길 때 검토한다.

## 도메인 모델

### User

서비스 내부 사용자다.

```txt
userId
displayName
userType: GUEST | MEMBER
createdAt
```

### AuthIdentity

외부 인증 제공자와 user를 연결한다.

```txt
authIdentityId
userId
provider: GUEST | GOOGLE | KAKAO | APPLE | GITHUB | COGNITO
providerSubject
createdAt
```

한 user가 여러 identity를 가질 수 있다.

예:

```txt
user-1
  -> GUEST: guest-session-abc
  -> GOOGLE: google-sub-123
  -> KAKAO: kakao-id-456
```

## AuthPrincipal

애플리케이션 내부에서는 인증 방식을 직접 보지 않고 `AuthPrincipal`만 본다.

```txt
AuthPrincipal
  userId
  authType
  provider
  providerSubject
```

이렇게 하면 guest, Cognito, Google, Kakao가 모두 같은 방식으로 core에 들어온다.

## WebSocket 인증

초기 구현:

```txt
ws://localhost:8080/ws/chat?roomId={roomId}&connectionId={connectionId}
```

목표 구현:

```txt
WebSocket 연결
-> HELLO message
-> token 또는 guest credential 검증
-> CONNECTED event
```

소셜 로그인 이후:

```txt
Authorization: Bearer <access-token>
```

브라우저 WebSocket에서 header 제약이 생기면 다음 후보를 검토한다.

- query parameter token
- cookie
- WebSocket subprotocol
- 연결 직후 HELLO payload

보안상 query token은 노출 위험이 있으므로 장기 기본값으로 두지 않는다.

## Guest -> Social 연결

중요한 UX:

```txt
게스트로 게임/채팅 시작
-> 나중에 Google/Kakao 로그인
-> 기존 guest userId 유지
-> AuthIdentity만 추가
```

이렇게 해야 로그인 전 채팅 기록, 게임 기록, 닉네임을 잃지 않는다.

## 구현 순서

1. `UserId` value object 추가
2. `User`, `AuthIdentity` 도메인 초안 추가
3. guest user 생성 API 추가
4. connectionId 발급 정책 추가
5. WebSocket 연결에서 userId/connectionId 분리
6. Spring Security는 아직 붙이지 않고 AuthPrincipal 추상화부터 도입
7. 이후 Cognito PoC

## 당장 결정하지 않을 것

- Cognito 최종 도입 여부
- Kakao/Google 중 첫 social provider
- MFA 정책
- 유료 auth provider 사용 여부
- 장기 token 저장 방식

이 결정들은 guest auth와 내부 AuthPrincipal 구조가 잡힌 뒤 해도 늦지 않다.
