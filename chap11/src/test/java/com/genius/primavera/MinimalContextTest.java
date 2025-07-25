package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.genius.primavera.domain.model")
class MinimalTestApplication {
}

@Slf4j
@SpringBootTest(classes = MinimalTestApplication.class, properties = {
    "spring.main.web-application-type=none",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never",
    "spring.flyway.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@ActiveProfiles("test")
class MinimalContextTest {

    @Test
    void contextLoads() {
        assertTrue(true);
        log.info("Minimal context test passed");
    }
}