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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * chap18 Front 마이크로서비스 통합 테스트 추상 클래스
 * 
 * 특징:
 * - Redis 컨테이너 (세션 관리)
 * - WebFlux 기반 API Gateway
 * - 마이크로서비스 통합 테스트 지원
 * - 외부 API 호출 테스트
 * - JUnit 5 PER_CLASS + CONCURRENT 지원
 * 
 * 사용법:
 * ```java
 * class FrontServiceTest extends AbstractIntegrationTest {
 *     @Test
 *     void shouldAggregateServiceCalls() {
 *         // 프론트엔드 API 통합 테스트
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
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Redis 설정
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        
        // WebFlux 설정
        registry.add("server.port", () -> "0");
        registry.add("spring.webflux.static-path-pattern", () -> "/static/**");
        
        // 마이크로서비스 연동 설정 (테스트용 목 서버)
        registry.add("services.account.url", () -> "http://localhost:8081");
        registry.add("services.product.url", () -> "http://localhost:8082");
        registry.add("services.order.url", () -> "http://localhost:8083");
        
        // 관리 엔드포인트
        registry.add("management.endpoints.web.exposure.include", () -> "health,info,metrics");
        
        log.info("🔴 Front Service - Redis 테스트 컨테이너: {}:{}", redis.getHost(), redis.getMappedPort(6379));
    }
}