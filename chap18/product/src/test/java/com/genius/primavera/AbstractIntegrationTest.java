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
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * chap18 Product 마이크로서비스 통합 테스트 추상 클래스
 * 
 * 특징:
 * - MariaDB 11.4 컨테이너 (상품 데이터)
 * - Redis 컨테이너 (상품 캐싱)
 * - Kafka 컨테이너 (재고 이벤트)
 * - 마이크로서비스 통합 테스트 지원
 * - JUnit 5 PER_CLASS + CONCURRENT 지원
 * 
 * 사용법:
 * ```java
 * class ProductServiceTest extends AbstractIntegrationTest {
 *     @Test
 *     void shouldManageProductInventory() {
 *         // 상품 관리 마이크로서비스 테스트
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
            .withDatabaseName("product_db")
            .withUsername("product_user")
            .withPassword("product_pass");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withExposedPorts(9092);
    
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
        
        // Kafka 설정
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "product-service-test");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        
        // 마이크로서비스 설정
        registry.add("server.port", () -> "0");
        registry.add("management.endpoints.web.exposure.include", () -> "health,info,metrics");
        
        log.info("🐳 Product Service - MariaDB 테스트 컨테이너: {}", mariadb.getJdbcUrl());
        log.info("🔴 Product Service - Redis 테스트 컨테이너: {}:{}", redis.getHost(), redis.getMappedPort(6379));
        log.info("📨 Product Service - Kafka 테스트 컨테이너: {}", kafka.getBootstrapServers());
    }
}