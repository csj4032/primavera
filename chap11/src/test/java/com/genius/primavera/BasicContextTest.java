package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver", 
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never",
    "spring.flyway.enabled=false",
    "logging.level.root=WARN",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@ActiveProfiles("test")
class BasicContextTest {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
        log.info("Spring context loaded successfully");
    }

    @Test
    void activeProfileTest() {
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        assertEquals(1, activeProfiles.length);
        assertEquals("test", activeProfiles[0]);
        log.info("Active profile: {}", activeProfiles[0]);
    }

    @Test
    void applicationNameTest() {
        String appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        assertEquals("primavera-test", appName);
        log.info("Application name: {}", appName);
    }
}