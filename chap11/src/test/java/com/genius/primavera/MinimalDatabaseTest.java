package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class,
    HttpEncodingAutoConfiguration.class,
    WebMvcAutoConfiguration.class
})
@EntityScan("com.genius.primavera.domain")
class TestConfig {
}

@Slf4j
@DataJpaTest
@SpringJUnitConfig(TestConfig.class)
@ActiveProfiles("test")
class MinimalDatabaseTest {

    @Test
    void contextLoads() {
        assertTrue(true);
        log.info("Minimal database test passed - context loads successfully");
    }
}