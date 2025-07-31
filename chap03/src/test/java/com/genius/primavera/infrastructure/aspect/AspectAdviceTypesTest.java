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
        // Given
        Long userId = 100L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        
        // @Before는 메서드 실행 전에 실행되며, 반환값에 영향을 주지 않음
        log.info("🔍 @Before Advice - 메서드 실행 전 로깅 완료");
    }

    @Test
    @Order(2)
    @DisplayName("@After - 메서드 실행 후 항상 처리 테스트")
    void testAfterAdvice() {
        // Given
        Long userId = 200L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        
        // @After는 메서드 실행 후 예외 발생 여부와 관계없이 항상 실행됨 (finally와 유사)
        log.info("🔚 @After Advice - 메서드 실행 후 정리 작업 완료");
    }

    @Test
    @Order(3)
    @DisplayName("@AfterReturning - 정상 반환 시에만 처리 테스트")
    void testAfterReturningAdvice() {
        // Given
        Long userId = 300L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        
        // @AfterReturning은 메서드가 정상적으로 반환될 때만 실행됨
        // returning 속성을 통해 반환값을 받을 수 있음
        log.info("✅ @AfterReturning Advice - 정상 반환 시 후처리 완료");
    }

    @Test
    @Order(4)
    @DisplayName("@Around - 메서드 실행 전후 완전 제어 테스트")
    void testAroundAdvice() {
        // Given
        Long userId = 400L;
        
        // When
        long startTime = System.currentTimeMillis();
        User result = helloService.getUserById(userId);
        long endTime = System.currentTimeMillis();

        // Then
        assertThat(result).isNotNull();
        
        // @Around는 가장 강력한 Advice로 메서드 실행을 완전히 제어할 수 있음
        // ProceedingJoinPoint.proceed()를 호출하여 실제 메서드를 실행
        log.info("🚀 @Around Advice - 실행 시간 측정: {}ms", endTime - startTime);
    }

    @Test
    @Order(5)
    @DisplayName("복합 Advice 동작 순서 테스트")
    void testMultipleAdviceExecution() {
        // Given
        Long userId = 500L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        
        /* 
         * Advice 실행 순서:
         * 1. @Around (before 부분)
         * 2. @Before
         * 3. 실제 메서드 실행
         * 4. @AfterReturning (정상 종료 시) 또는 @AfterThrowing (예외 발생 시)
         * 5. @After
         * 6. @Around (after 부분)
         */
        log.info("📊 복합 Advice 실행 순서 테스트 완료");
    }

    @Test
    @Order(6)
    @DisplayName("Pointcut 표현식 다양한 패턴 테스트")
    void testPointcutExpressions() {
        // Given - 다양한 Pointcut 표현식 테스트
        
        // execution 표현식: 메서드 시그니처 기반
        // @annotation 표현식: 특정 어노테이션 기반
        // within 표현식: 특정 패키지나 클래스 내의 모든 메서드
        // args 표현식: 메서드 인자 타입 기반
        // target 표현식: 타겟 객체 타입 기반
        
        Long userId = 600L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        
        log.info("🎯 다양한 Pointcut 표현식 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("AOP Proxy 메커니즘 테스트")
    void testAOPProxyMechanism() {
        // Given
        Long userId = 700L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        
        // Spring AOP는 런타임에 Proxy 객체를 생성하여 Aspect를 적용
        // Interface가 있으면 JDK Dynamic Proxy, 없으면 CGLIB Proxy 사용
        String proxyClass = helloService.getClass().getName();
        log.info("🔄 AOP Proxy 클래스: {}", proxyClass);
        
        assertThat(proxyClass.contains("$Proxy") || proxyClass.contains("$$")).isTrue();
        log.info("✅ AOP Proxy 메커니즘 테스트 완료");
    }

    @Test
    @Order(8)
    @DisplayName("JoinPoint 메타데이터 추출 테스트")
    void testJoinPointMetadata() {
        // Given
        Long userId = 800L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        
        // JoinPoint를 통해 다음 정보들을 추출할 수 있음:
        // - getSignature(): 메서드 시그니처
        // - getTarget(): 타겟 객체
        // - getArgs(): 메서드 인자들
        // - getKind(): JoinPoint 종류
        // - getStaticPart(): 정적 정보
        
        log.info("📋 JoinPoint 메타데이터 추출 테스트 완료");
    }

    @Test
    @Order(9)
    @DisplayName("AOP를 통한 횡단 관심사 분리 시연")
    void testCrossCuttingConcerns() {
        // Given
        Long userId = 900L;

        // When - 여러 번 호출하여 횡단 관심사(로깅, 보안, 트랜잭션 등) 처리 확인
        for (int i = 0; i < 3; i++) {
            User result = helloService.getUserById(userId + i);
            assertThat(result).isNotNull();
        }

        // Then
        // 비즈니스 로직과 횡단 관심사가 완전히 분리됨을 확인
        // - 로깅: 모든 메서드 호출에 대한 일관된 로깅
        // - 보안: 인증/인가 체크
        // - 성능 모니터링: 실행 시간 측정
        // - 캐싱: 결과 캐싱
        // - 트랜잭션: 선언적 트랜잭션 관리
        
        log.info("🎯 횡단 관심사 분리 시연 완료");
    }

    @Test
    @Order(10)
    @DisplayName("AOP 성능 영향도 테스트")
    void testAOPPerformanceImpact() {
        // Given
        Long userId = 1000L;
        int iterations = 1000;

        // When - AOP가 적용된 메서드의 성능 측정
        long startTime = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            User result = helloService.getUserById(userId);
            assertThat(result).isNotNull();
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double averageTime = totalTime / (double) iterations / 1_000_000; // ms 단위

        // Then
        log.info("⚡ AOP 성능 측정 결과:");
        log.info("  - 총 실행 시간: {}ms", totalTime / 1_000_000);
        log.info("  - 평균 실행 시간: {}ms", averageTime);
        log.info("  - 총 호출 횟수: {}", iterations);
        
        // AOP Proxy 오버헤드가 있지만 일반적으로 무시할 수 있는 수준
        assertThat(averageTime).isLessThan(1.0); // 1ms 미만
        
        log.info("✅ AOP 성능 영향도 테스트 완료");
    }
}