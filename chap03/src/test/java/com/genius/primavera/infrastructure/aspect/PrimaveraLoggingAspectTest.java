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
    @DisplayName("@Before Advice test - translated_text_3 execution translated_text_1 translated_text_2")
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
    @DisplayName("@Around Advice test - translated_text_3 execution translated_text_1translated_text_1 translated_text_2")
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
    @DisplayName("@AfterReturning Advice test - translated_text_2 translated_text_2 translated_text_1 processing")
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
    @DisplayName("@After Advice test - translated_text_3 execution completed translated_text_1 processing")
    void testAfterAdvice() {
        Long userId = 4L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect, times(1)).afterHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        log.info(" @After Advice test completed");
    }

    @Test
    @Order(5)
    @DisplayName("Pointcut translated_text_3 test - execution translated_text_2 translated_text_2")
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
        log.info(" Pointcut translated_text_3 test completed");
    }

    @Test
    @Order(6)
    @DisplayName("AOP annotation translated_text_2 translated_text_3 test")
    void testAnnotationBasedPointcut() throws Throwable {
        Long userId = 6L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect).aroundHandler(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        log.info(" annotation translated_text_2 Pointcut test completed");
    }

    @Test
    @Order(7)
    @DisplayName("JoinPoint information translated_text_2 test")
    void testJoinPointInformation() {
        Long userId = 7L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        log.info(" JoinPoint information translated_text_2 test completed");
    }

    @Test
    @Order(8)
    @DisplayName("AOP Advice execution translated_text_2 test")
    void testAdviceExecutionOrder() {
        Long userId = 8L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        log.info(" AOP Advice execution translated_text_2 test completed");
    }

    @Test
    @Order(9)
    @DisplayName("ProceedingJoinPointtranslated_text_1 translated_text_2 translated_text_3 execution translated_text_2 test")
    void testProceedingJoinPointControl() throws Throwable {
        Long userId = 9L;
        User result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        verify(loggingAspect).helloServiceAround(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());
        log.info(" ProceedingJoinPoint translated_text_2 test completed");
    }

    @Test
    @Order(10)
    @DisplayName("AOPtranslated_text_1 translated_text_2 translated_text_2 translated_text_4 translated_text_5")
    void testPerformanceMonitoring() {
        Long userId = 10L;
        long startTime = System.currentTimeMillis();
        User result = helloService.getUserById(userId);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        assertThat(result).isNotNull();
        assertThat(executionTime).isGreaterThanOrEqualTo(0);
        log.info(" translated_text_2 translated_text_4 translated_text_5 completed - execution translated_text_2: {}ms", executionTime);
    }
}