package io.wowtalk.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "wow-talk API",
                version = "v1",
                description = "WebSocket 우선 구조로 시작하는 wow-talk 백엔드 API 문서",
                contact = @Contact(name = "wow-talk backend")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "local"),
                @Server(url = "http://localhost:18080", description = "local-temp")
        }
)
public class OpenApiConfig {
}
