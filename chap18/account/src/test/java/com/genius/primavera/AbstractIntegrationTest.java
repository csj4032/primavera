package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * chap18 Account 마이크로서비스 통합 테스트 추상 클래스
 * 
 * 특징:
 * - MariaDB 11.4 컨테이너 (사용자 계정 데이터)
 * - Redis 컨테이너 (세션 및 캐싱)
 * - 마이크로서비스 통합 테스트 지원
 * - JUnit 5 PER_CLASS + CONCURRENT 지원
 * 
 * 사용법:
 * ```java
 * class AccountServiceTest extends AbstractIntegrationTest {
 *     @Test
 *     void shouldManageUserAccount() {
 *         // 계정 관리 마이크로서비스 테스트
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
            .withDatabaseName("account_db")
            .withUsername("account_user")
            .withPassword("account_pass");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MariaDB 설정
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
        
        // Redis 설정
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        
        // 마이크로서비스 설정
        registry.add("server.port", () -> "0"); // 랜덤 포트
        registry.add("management.endpoints.web.exposure.include", () -> "health,info,metrics");
        
        log.info("🐳 Account Service - MariaDB 테스트 컨테이너: {}", mariadb.getJdbcUrl());
        log.info("🔴 Account Service - Redis 테스트 컨테이너: {}:{}", redis.getHost(), redis.getMappedPort(6379));
    }
}