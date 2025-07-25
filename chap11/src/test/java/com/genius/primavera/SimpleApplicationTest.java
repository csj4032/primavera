package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootApplication
@ComponentScan(excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Security.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*OAuth.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Filter.*")
})
class SimpleTestApplication {
}

@Slf4j
@SpringBootTest(classes = SimpleTestApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never",
    "spring.flyway.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration,org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration,org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration"
})
@ActiveProfiles("test")
class SimpleApplicationTest {

    @Test
    void contextLoads() {
        assertTrue(true);
        log.info("Simple application test passed");
    }
}