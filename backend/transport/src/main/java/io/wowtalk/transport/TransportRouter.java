package io.wowtalk.transport;

public interface TransportRouter {

    ChatTransport route(TransportMode transportMode);
}
