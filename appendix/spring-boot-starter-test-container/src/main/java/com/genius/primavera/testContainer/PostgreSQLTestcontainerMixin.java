package com.genius.primavera.testContainer;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * PostgreSQL TestContainer Mixin
 * 
 * 사용법:
 * @SpringBootTest
 * public class MyTest implements PostgreSQLTestcontainerMixin {
 *     @Autowired
 *     private DataSource dataSource; // 자동으로 PostgreSQL 컨테이너에 연결됨
 * }
 */
@Testcontainers
public interface PostgreSQLTestcontainerMixin {
    
    @Container
    PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configurePostgreSQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
    
    default String getPostgreSQLJdbcUrl() {
        return postgresql.getJdbcUrl();
    }
}