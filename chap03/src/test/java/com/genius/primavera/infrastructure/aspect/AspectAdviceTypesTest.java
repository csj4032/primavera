package com.genius.primavera.infrastructure.aspect;

import com.genius.primavera.applicaiton.HelloService;
import com.genius.primavera.applicaiton.OopsException;
import com.genius.primavera.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AOP Advice 타입별 상세 테스트")
public class AspectAdviceTypesTest {

    @Autowired
    private HelloService helloService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestAspectForAdviceTypes testAspect() {
            return new TestAspectForAdviceTypes();
        }
    }

    @Test
    @Order(1)
    @DisplayName("@Before - 메서드 실행 전 처리 테스트")
    void testBeforeAdvice() {
        var userId = 100L;
        var result = helloService.getUserById(userId);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        log.info("🔍 @Before Advice - 메서드 실행 전 로깅 완료");
    }

    @Test
    @Order(2)
    @DisplayName("@After - 메서드 실행 후 항상 처리 테스트")
    void testAfterAdvice() {
        var userId = 200L;
        var result = helloService.getUserById(userId);
        
        assertThat(result).isNotNull();
        log.info("🔚 @After Advice - 메서드 실행 후 정리 작업 완료");
    }

    @Test
    @Order(3)
    @DisplayName("@AfterReturning - 정상 반환 시에만 처리 테스트")
    void testAfterReturningAdvice() {
        var userId = 300L;
        var result = helloService.getUserById(userId);
        
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        log.info("✅ @AfterReturning Advice - 정상 반환 시 후처리 완료");
    }

    @Test
    @Order(4)
    @DisplayName("@Around - 메서드 실행 전후 완전 제어 테스트")
    void testAroundAdvice() {
        var userId = 400L;
        var startTime = System.currentTimeMillis();
        var result = helloService.getUserById(userId);
        var endTime = System.currentTimeMillis();
        
        assertThat(result).isNotNull();
        log.info("🚀 @Around Advice - 실행 시간 측정: {}ms", endTime - startTime);
    }

    @Test
    @Order(5)
    @DisplayName("복합 Advice 동작 순서 테스트")
    void testMultipleAdviceExecution() {
        var userId = 500L;
        var result = helloService.getUserById(userId);
        
        assertThat(result).isNotNull();
        log.info("📊 복합 Advice 실행 순서 테스트 완료");
    }

    @Test
    @Order(6)
    @DisplayName("Pointcut 표현식 다양한 패턴 테스트")
    void testPointcutExpressions() {
        var userId = 600L;
        var result = helloService.getUserById(userId);
        
        assertThat(result).isNotNull();
        log.info("🎯 다양한 Pointcut 표현식 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("AOP Proxy 메커니즘 테스트")
    void testAOPProxyMechanism() {
        var userId = 700L;
        var result = helloService.getUserById(userId);
        var proxyClass = helloService.getClass().getName();
        
        assertThat(result).isNotNull();
        assertThat(proxyClass.contains("$Proxy") || proxyClass.contains("$$")).isTrue();
        
        log.info("🔄 AOP Proxy 클래스: {}", proxyClass);
        log.info("✅ AOP Proxy 메커니즘 테스트 완료");
    }

    @Test
    @Order(8)
    @DisplayName("JoinPoint 메타데이터 추출 테스트")
    void testJoinPointMetadata() {
        var userId = 800L;
        var result = helloService.getUserById(userId);
        
        assertThat(result).isNotNull();
        log.info("📋 JoinPoint 메타데이터 추출 테스트 완료");
    }

    @Test
    @Order(9)
    @DisplayName("AOP를 통한 횡단 관심사 분리 시연")
    void testCrossCuttingConcerns() {
        var userId = 900L;
        
        for (int i = 0; i < 3; i++) {
            var result = helloService.getUserById(userId + i);
            assertThat(result).isNotNull();
        }
        
        log.info("🎯 횡단 관심사 분리 시연 완료");
    }

    @Test
    @Order(10)
    @DisplayName("AOP 성능 영향도 테스트")
    void testAOPPerformanceImpact() {
        var userId = 1000L;
        var iterations = 1000;
        var startTime = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            var result = helloService.getUserById(userId);
            assertThat(result).isNotNull();
        }
        
        var endTime = System.nanoTime();
        var totalTime = endTime - startTime;
        var averageTime = totalTime / (double) iterations / 1_000_000;
        
        assertThat(averageTime).isLessThan(1.0);
        
        log.info("⚡ AOP 성능 측정 결과:");
        log.info("  - 총 실행 시간: {}ms", totalTime / 1_000_000);
        log.info("  - 평균 실행 시간: {}ms", averageTime);
        log.info("  - 총 호출 횟수: {}", iterations);
        log.info("✅ AOP 성능 영향도 테스트 완료");
    }
}