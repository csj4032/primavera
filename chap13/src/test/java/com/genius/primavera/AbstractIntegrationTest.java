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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * chap13 고급 권한 관리 통합 테스트 추상 클래스
 * 
 * 특징:
 * - MariaDB 11.4 컨테이너 (관계형 데이터)
 * - MongoDB 7.0 컨테이너 (문서형 데이터)
 * - 복합 데이터 저장소 테스트 지원
 * - JUnit 5 PER_CLASS + CONCURRENT 지원
 * 
 * 사용법:
 * ```java
 * class UserAuthorizationIntegrationTest extends AbstractIntegrationTest {
 *     @Test
 *     void shouldManageUserPermissions() {
 *         // MariaDB + MongoDB 동시 사용 가능
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
    
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MariaDB 설정
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
        
        // MongoDB 설정
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
        
        log.info("🐳 MariaDB 테스트 컨테이너 설정 완료: {}", mariadb.getJdbcUrl());
        log.info("🍃 MongoDB 테스트 컨테이너 설정 완료: {}", mongodb.getReplicaSetUrl());
    }
}