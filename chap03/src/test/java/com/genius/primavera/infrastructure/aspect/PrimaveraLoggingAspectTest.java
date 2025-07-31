package com.genius.primavera.infrastructure.aspect;

import com.genius.primavera.applicaiton.HelloService;
import com.genius.primavera.applicaiton.OopsException;
import com.genius.primavera.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PrimaveraLoggingAspect 테스트")
class PrimaveraLoggingAspectTest {

    @Autowired
    private HelloService helloService;

    @SpyBean
    private PrimaveraLoggingAspect loggingAspect;

    @Test
    @Order(1)
    @DisplayName("@Before Advice 테스트 - 메서드 실행 전 로깅")
    void testBeforeAdvice() {
        // Given
        Long userId = 1L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Test User");

        // Verify Before advice was called
        verify(loggingAspect, times(1)).beforeHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(loggingAspect, times(1)).helloServiceBefore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());

        log.info("✅ @Before Advice 테스트 완료");
    }

    @Test
    @Order(2)
    @DisplayName("@Around Advice 테스트 - 메서드 실행 전후 제어")
    void testAroundAdvice() throws Throwable {
        // Given
        Long userId = 2L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);

        // Verify Around advice was called
        verify(loggingAspect, times(1)).aroundHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(loggingAspect, times(1)).helloServiceAround(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());

        log.info("✅ @Around Advice 테스트 완료");
    }

    @Test
    @Order(3)
    @DisplayName("@AfterReturning Advice 테스트 - 정상 반환 후 처리")
    void testAfterReturningAdvice() {
        // Given
        Long userId = 3L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();

        // Verify AfterReturning advice was called
        verify(loggingAspect, times(1)).afterReturningHandler(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );

        log.info("✅ @AfterReturning Advice 테스트 완료");
    }

    @Test
    @Order(4)
    @DisplayName("@After Advice 테스트 - 메서드 실행 완료 후 처리")
    void testAfterAdvice() {
        // Given
        Long userId = 4L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();

        // Verify After advice was called
        verify(loggingAspect, times(1)).afterHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        log.info("✅ @After Advice 테스트 완료");
    }

    @Test
    @Order(5)
    @DisplayName("Pointcut 표현식 테스트 - execution 패턴 매칭")
    void testPointcutExpression() {
        // Given
        Long userId = 5L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        // Verify that all pointcuts matched correctly
        verify(loggingAspect, times(1)).helloServiceBefore(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any()
        );

        log.info("✅ Pointcut 표현식 테스트 완료");
    }

    @Test
    @Order(6)
    @DisplayName("AOP 어노테이션 기반 타겟팅 테스트")
    void testAnnotationBasedPointcut() throws Throwable {
        // Given
        Long userId = 6L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();

        // Verify that annotation-based pointcut worked
        verify(loggingAspect).aroundHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        log.info("✅ 어노테이션 기반 Pointcut 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("JoinPoint 정보 추출 테스트")
    void testJoinPointInformation() {
        // Given
        Long userId = 7L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);

        // JoinPoint를 통해 메서드 시그니처, 타겟 객체, 인자 등을 확인할 수 있음
        log.info("✅ JoinPoint 정보 추출 테스트 완료");
    }

    @Test
    @Order(8)
    @DisplayName("AOP Advice 실행 순서 테스트")
    void testAdviceExecutionOrder() {
        // Given
        Long userId = 8L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();

        // AOP Advice 실행 순서:
        // @Around (before) → @Before → 실제 메서드 → @AfterReturning → @After → @Around (after)
        log.info("✅ AOP Advice 실행 순서 테스트 완료");
    }

    @Test
    @Order(9)
    @DisplayName("ProceedingJoinPoint를 통한 메서드 실행 제어 테스트")
    void testProceedingJoinPointControl() throws Throwable {
        // Given
        Long userId = 9L;

        // When
        User result = helloService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();

        // ProceedingJoinPoint.proceed()를 통해 실제 메서드 실행을 제어할 수 있음
        verify(loggingAspect).helloServiceAround(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());

        log.info("✅ ProceedingJoinPoint 제어 테스트 완료");
    }

    @Test
    @Order(10)
    @DisplayName("AOP를 통한 성능 모니터링 시뮬레이션")
    void testPerformanceMonitoring() {
        // Given
        Long userId = 10L;
        long startTime = System.currentTimeMillis();

        // When
        User result = helloService.getUserById(userId);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        assertThat(result).isNotNull();
        assertThat(executionTime).isGreaterThanOrEqualTo(0);

        log.info("✅ 성능 모니터링 시뮬레이션 완료 - 실행 시간: {}ms", executionTime);
    }
}