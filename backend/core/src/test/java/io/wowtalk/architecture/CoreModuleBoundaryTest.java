package io.wowtalk.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoreModuleBoundaryTest {

    /*
     * This project intentionally keeps backend/core free of adapter implementations.
     * A lightweight source-level guard is enough here because the rule is about imports,
     * not runtime behavior.
     */
    private static final List<String> FORBIDDEN_IMPORTS = List.of(
            "jakarta.persistence.",
            "org.springframework.dao.",
            "io.wowtalk.dynamodb.",
            "io.wowtalk.postgres.",
            "io.wowtalk.redis."
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

    private boolean containsForbiddenImport(Path path) {
        try {
            String source = Files.readString(path);
            return FORBIDDEN_IMPORTS.stream().anyMatch(source::contains);
        } catch (IOException exception) {
            throw new IllegalStateException("소스 파일을 읽을 수 없습니다: " + path, exception);
        }
    }
}
