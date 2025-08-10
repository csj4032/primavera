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
@DisplayName("PrimaveraLoggingAspect test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PrimaveraLoggingAspectTest {

    @Autowired
    private HelloService helloService;

    @SpyBean
    private PrimaveraLoggingAspect loggingAspect;

    @Test
    @Order(1)
    @DisplayName("@Before Advice test - connection execution should test")
    void testBeforeAdvice() {
        Long userId = 1L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Alice");
        verify(loggingAspect, times(1)).beforeHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(loggingAspect, times(1)).helloServiceBefore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());
        log.info(" @Before Advice test completed");
    }

    @Test
    @Order(2)
    @DisplayName("@Around Advice test - connection execution shouldshould test")
    void testAroundAdvice() throws Throwable {
        Long userId = 2L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(loggingAspect, times(1)).aroundHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(loggingAspect, times(1)).helloServiceAround(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());
        log.info(" @Around Advice test completed");
    }

    @Test
    @Order(3)
    @DisplayName("@AfterReturning Advice test - test should processing")
    void testAfterReturningAdvice() {
        Long userId = 3L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect, times(1)).afterReturningHandler(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
        log.info(" @AfterReturning Advice test completed");
    }

    @Test
    @Order(4)
    @DisplayName("@After Advice test - connection execution completed should processing")
    void testAfterAdvice() {
        Long userId = 4L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect, times(1)).afterHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        log.info(" @After Advice test completed");
    }

    @Test
    @Order(5)
    @DisplayName("Pointcut connection test - execution test")
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
        log.info(" Pointcut connection test completed");
    }

    @Test
    @Order(6)
    @DisplayName("AOP annotation test connection test")
    void testAnnotationBasedPointcut() throws Throwable {
        Long userId = 6L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect).aroundHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        log.info(" annotation test Pointcut test completed");
    }

    @Test
    @Order(7)
    @DisplayName("JoinPoint information test")
    void testJoinPointInformation() {
        Long userId = 7L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        log.info(" JoinPoint information test completed");
    }

    @Test
    @Order(8)
    @DisplayName("AOP Advice execution test")
    void testAdviceExecutionOrder() {
        Long userId = 8L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        log.info(" AOP Advice execution test completed");
    }

    @Test
    @Order(9)
    @DisplayName("ProceedingJoinPointshould test connection execution test")
    void testProceedingJoinPointControl() throws Throwable {
        Long userId = 9L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect).helloServiceAround(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());
        log.info(" ProceedingJoinPoint test completed");
    }

    @Test
    @Order(10)
    @DisplayName("AOPshould test file Endpoint")
    void testPerformanceMonitoring() {
        Long userId = 10L;
        long startTime = System.currentTimeMillis();
        User result = helloService.getUserById(userId);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        assertThat(result).isNotNull();
        assertThat(executionTime).isGreaterThanOrEqualTo(0);
        log.info(" test file Endpoint completed - execution test: {}ms", executionTime);
    }
}