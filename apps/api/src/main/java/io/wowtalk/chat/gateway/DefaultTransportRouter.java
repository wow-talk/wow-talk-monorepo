package io.wowtalk.chat.gateway;

import io.wowtalk.transport.ChatTransport;
import io.wowtalk.transport.TransportMode;
import io.wowtalk.transport.TransportRouter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DefaultTransportRouter implements TransportRouter {

    private final Map<TransportMode, ChatTransport> transports;

    public DefaultTransportRouter(List<ChatTransport> transports) {
        this.transports = new EnumMap<>(TransportMode.class);

        for (ChatTransport transport : transports) {
            ChatTransport previous = this.transports.put(transport.mode(), transport);
            if (previous != null) {
                throw new IllegalStateException("중복된 transport 구현이 등록되었습니다: " + transport.mode());
            }
        }
    }

    @Override
    public ChatTransport route(TransportMode transportMode) {
        ChatTransport transport = transports.get(transportMode);
        if (transport == null) {
            throw new IllegalStateException("지원하지 않는 transport 모드입니다: " + transportMode);
        }
        return transport;
    }
}
