/**
 * 실행 앱에서 transport 구현체와 realtime publisher를 조합하는 gateway 패키지다.
 *
 * <p>core가 WebSocket, Redis 같은 구현체를 직접 알지 않도록 어댑터 선택 책임을 이 계층에 둔다.
 */
package io.wowtalk.chat.gateway;
