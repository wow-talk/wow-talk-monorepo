package io.wowtalk.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoreModuleBoundaryTest {

    /*
     * backend/core는 adapter 구현을 몰라야 한다.
     * 이 규칙은 런타임 동작보다 import 방향을 지키는 문제라서, 소스 레벨 검사로 가볍게 막는다.
     */
    private static final List<String> FORBIDDEN_IMPORTS = List.of(
            "jakarta.persistence.",
            "org.springframework.dao.",
            "io.wowtalk.dynamodb.",
            "io.wowtalk.postgres.",
            "io.wowtalk.redis."
    );

    private static final List<String> FORBIDDEN_PACKAGE_PATHS = List.of(
            "io/wowtalk/dynamodb",
            "io/wowtalk/postgres",
            "io/wowtalk/redis",
            "io/wowtalk/websocket"
    );

    @Test
    void core는_저장소_구현체와_영속성_기술에_의존하지_않는다() throws IOException {
        Path sourceRoot = Path.of("src/main/java");

        List<Path> violatingFiles;
        try (var stream = Files.walk(sourceRoot)) {
            violatingFiles = stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::containsForbiddenImport)
                    .toList();
        }

        assertThat(violatingFiles)
                .as("backend/core must not depend on adapter modules or persistence implementation APIs")
                .isEmpty();
    }

    @Test
    void core는_adapter_패키지_이름도_소유하지_않는다() throws IOException {
        Path sourceRoot = Path.of("src/main/java");

        List<Path> violatingPaths;
        try (var stream = Files.walk(sourceRoot)) {
            violatingPaths = stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isForbiddenPackagePath)
                    .toList();
        }

        assertThat(violatingPaths)
                .as("backend/core must not own adapter-specific packages")
                .isEmpty();
    }

    private boolean containsForbiddenImport(Path path) {
        try {
            String source = Files.readString(path);
            return FORBIDDEN_IMPORTS.stream().anyMatch(source::contains);
        } catch (IOException exception) {
            throw new IllegalStateException("소스 파일을 읽을 수 없습니다: " + path, exception);
        }
    }

    private boolean isForbiddenPackagePath(Path path) {
        String normalizedPath = path.toString().replace('\\', '/');
        return FORBIDDEN_PACKAGE_PATHS.stream().anyMatch(normalizedPath::contains);
    }
}
