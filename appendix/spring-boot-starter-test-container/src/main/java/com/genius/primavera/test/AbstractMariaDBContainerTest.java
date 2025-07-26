package com.genius.primavera.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public abstract class AbstractMariaDBContainerTest {

    @Container
    protected static final MariaDBContainer<?> mariadb = createMariaDBContainer();

    private static MariaDBContainer<?> createMariaDBContainer() {
        Class<?> testClass = MariaDBContainerFactory.findTestClass();
        if (testClass != null) {
            return MariaDBContainerFactory.createFromAnnotation(testClass);
        }

        MariaDBContainer<?> container = new MariaDBContainer<>("mariadb:11.4.7")
                .withDatabaseName("primavera")
                .withUsername("primavera")
                .withPassword("primavera")
                .withCommand("--default-authentication-plugin=mysql_native_password");

        String initScript = getInitScript();
        if (initScript.isBlank() && !initScript.trim().isEmpty() && !"none".equals(initScript)) {
            container.withInitScript(initScript);
        }

        return container;
    }

    protected static String getInitScript() {
        return "sql/schema.sql";
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = mariadb.getJdbcUrl() + "?allowPublicKeyRetrieval=true&useSSL=false";
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;
}