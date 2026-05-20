package io.wowtalk;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRepositoryConfig.class)
class WowTalkApplicationTests {

    @Test
    void contextLoads() {
    }

}
