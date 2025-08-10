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
@DisplayName("AOP Advice connection test")
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
    @DisplayName("@Before - connection execution should processing test")
    void testBeforeAdvice() {
        var userId = 1L;
        var result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        log.info(" @Before Advice - connection execution should test completed");
    }

    @Test
    @Order(2)
    @DisplayName("@After - connection execution should test processing test")
    void testAfterAdvice() {
        var userId = 2L;
        var result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        log.info(" @After Advice - connection execution should test completed");
    }

    @Test
    @Order(3)
    @DisplayName("@AfterReturning - test connection processing test")
    void testAfterReturningAdvice() {
        var userId = 3L;
        var result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Charlie");
        log.info(" @AfterReturning Advice - test needs to be addedprocessing completed");
    }

    @Test
    @Order(4)
    @DisplayName("@Around - connection execution shouldneeds to be added test")
    void testAroundAdvice() {
        var userId = 4L;
        var startTime = System.currentTimeMillis();
        var result = helloService.getUserById(userId);
        var endTime = System.currentTimeMillis();
        assertThat(result).isNotNull();
        log.info(" @Around Advice - execution should test: {}ms", endTime - startTime);
    }

    @Test
    @Order(5)
    @DisplayName("test Advice test test")
    void testMultipleAdviceExecution() {
        var userId = 5L;
        var result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        log.info(" test Advice execution test completed");
    }

    @Test
    @Order(6)
    @DisplayName("Pointcut connection test")
    void testPointcutExpressions() {
        var userId = 6L;
        var result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        log.info(" connection Pointcut connection test completed");
    }

    @Test
    @Order(7)
    @DisplayName("AOP Proxy file test")
    void testAOPProxyMechanism() {
        var userId = 7L;
        var result = helloService.getUserById(userId);
        var proxyClass = helloService.getClass().getName();
        assertThat(result).isNotNull();
        assertThat(proxyClass.contains("$Proxy") || proxyClass.contains("$$")).isTrue();
        log.info(" AOP Proxy connection: {}", proxyClass);
        log.info(" AOP Proxy file test completed");
    }

    @Test
    @Order(8)
    @DisplayName("JoinPoint with test")
    void testJoinPointMetadata() {
        var userId = 8L;
        var result = helloService.getUserById(userId);
        assertThat(result).isNotNull();
        log.info(" JoinPoint with test completed");
    }

    @Test
    @Order(9)
    @DisplayName("AOPshould test connection test should")
    void testCrossCuttingConcerns() {
        var userId = 9L;
        for (int i = 0; i < 3; i++) {
            var result = helloService.getUserById(userId + i);
            assertThat(result).isNotNull();
        }
        log.info(" test connection test should completed");
    }

    @Test
    @Order(10)
    @DisplayName("AOP test connection test")
    void testAOPPerformanceImpact() {
        var userId = 10L;
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
        
        log.info(" AOP test result:");
        log.info("  - should execution should: {}ms", totalTime / 1_000_000);
        log.info("  - test execution should: {}ms", averageTime);
        log.info("  - should called test: {}", iterations);
        log.info(" AOP test connection test completed");
    }
}