package io.wowtalk.transport;

/**
 * transportMode에 맞는 transport 구현체를 선택하는 조합 지점이다.
 *
 * <p>새 transport를 추가해도 core service가 구현체를 직접 참조하지 않게 하기 위해,
 * 실행 앱 쪽에서 구현체 목록을 모아 라우팅한다.
 */
public interface TransportRouter {

    ChatTransport route(TransportMode transportMode);
}
