package com.autoadda.apis.other;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Boots the full context against in-memory H2 so broken JPA mappings, repository method
 * names and bean wiring fail the build. Native MySQL queries are not executed here.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextTest {

    @Test
    void contextLoads() {
    }
}
