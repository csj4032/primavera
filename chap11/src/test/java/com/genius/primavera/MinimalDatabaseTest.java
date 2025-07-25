package com.genius.primavera;

import com.genius.primavera.config.BaseTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DataJpaTest
@Import(BaseTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class MinimalDatabaseTest {

    @Test
    void contextLoads() {
        assertTrue(true);
        log.info("Minimal database test passed - context loads successfully with MySQL container");
    }
}