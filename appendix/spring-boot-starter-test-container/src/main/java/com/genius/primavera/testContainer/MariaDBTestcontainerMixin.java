package com.genius.primavera.testContainer;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 간단한 MariaDB TestContainer Mixin
 * 
 * 사용법:
 * @SpringBootTest
 * public class MyTest implements MariaDBTestcontainerMixin {
 *     @Autowired
 *     private DataSource dataSource; // 자동으로 MariaDB 컨테이너에 연결됨
 * }
 */
@Testcontainers
public interface MariaDBTestcontainerMixin {
    
    @Container
    MariaDBContainer<?> mariadb = new MariaDBContainer<>(DockerImageName.parse("mariadb:11.4.7"))
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera");

    @DynamicPropertySource
    static void configureMariaDBProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
    }
    
    default String getMariaDBJdbcUrl() {
        return mariadb.getJdbcUrl();
    }
    
    default String getMariaDBHost() {
        return mariadb.getHost();
    }
    
    default Integer getMariaDBPort() {
        return mariadb.getMappedPort(3306);
    }
}