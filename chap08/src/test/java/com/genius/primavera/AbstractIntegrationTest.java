package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * chap08 보안 기초 통합 테스트 추상 클래스
 * 
 * 특징:
 * - MariaDB 11.4 컨테이너 제공
 * - Spring Security 통합 테스트 지원
 * - JUnit 5 PER_CLASS + CONCURRENT 지원
 * 
 * 사용법:
 * ```java
 * class UserServiceIntegrationTest extends AbstractIntegrationTest {
 *     @Test
 *     void shouldCreateUser() {
 *         // MariaDB 컨테이너 자동 사용 가능
 *     }
 * }
 * ```
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public abstract class AbstractIntegrationTest {
    
    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
        
        log.info("🐳 MariaDB 테스트 컨테이너 설정 완료: {}", mariadb.getJdbcUrl());
    }
}