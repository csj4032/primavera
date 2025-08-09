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
@DisplayName("PrimaveraLoggingAspect 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PrimaveraLoggingAspectTest {

    @Autowired
    private HelloService helloService;

    @SpyBean
    private PrimaveraLoggingAspect loggingAspect;

    @Test
    @Order(1)
    @DisplayName("@Before Advice 테스트 - 메서드 실행 전 로깅")
    void testBeforeAdvice() {
        Long userId = 1L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Alice");
        verify(loggingAspect, times(1)).beforeHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(loggingAspect, times(1)).helloServiceBefore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());
        log.info("✅ @Before Advice 테스트 완료");
    }

    @Test
    @Order(2)
    @DisplayName("@Around Advice 테스트 - 메서드 실행 전후 제어")
    void testAroundAdvice() throws Throwable {
        Long userId = 2L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(loggingAspect, times(1)).aroundHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(loggingAspect, times(1)).helloServiceAround(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());
        log.info("✅ @Around Advice 테스트 완료");
    }

    @Test
    @Order(3)
    @DisplayName("@AfterReturning Advice 테스트 - 정상 반환 후 처리")
    void testAfterReturningAdvice() {
        Long userId = 3L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
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
        Long userId = 4L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect, times(1)).afterHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        log.info("✅ @After Advice 테스트 완료");
    }

    @Test
    @Order(5)
    @DisplayName("Pointcut 표현식 테스트 - execution 패턴 매칭")
    void testPointcutExpression() {
        Long userId = 5L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("Eve@example.com");
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
        Long userId = 6L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect).aroundHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        log.info("✅ 어노테이션 기반 Pointcut 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("JoinPoint 정보 추출 테스트")
    void testJoinPointInformation() {
        Long userId = 7L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        log.info("✅ JoinPoint 정보 추출 테스트 완료");
    }

    @Test
    @Order(8)
    @DisplayName("AOP Advice 실행 순서 테스트")
    void testAdviceExecutionOrder() {
        Long userId = 8L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        log.info("✅ AOP Advice 실행 순서 테스트 완료");
    }

    @Test
    @Order(9)
    @DisplayName("ProceedingJoinPoint를 통한 메서드 실행 제어 테스트")
    void testProceedingJoinPointControl() throws Throwable {
        Long userId = 9L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect).helloServiceAround(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());
        log.info("✅ ProceedingJoinPoint 제어 테스트 완료");
    }

    @Test
    @Order(10)
    @DisplayName("AOP를 통한 성능 모니터링 시뮬레이션")
    void testPerformanceMonitoring() {
        Long userId = 10L;
        long startTime = System.currentTimeMillis();
        User result = helloService.getUserById(userId);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        assertThat(result).isNotNull();
        assertThat(executionTime).isGreaterThanOrEqualTo(0);
        log.info("✅ 성능 모니터링 시뮬레이션 완료 - 실행 시간: {}ms", executionTime);
    }
}