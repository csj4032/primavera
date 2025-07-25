package com.genius.primavera.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@DisplayName("AnnotationClass 라이프사이클 테스트")
class AnnotationClassTest {

    @Test
    @DisplayName("Bean 생성 시 @PostConstruct가 정상적으로 호출된다 (Given-When-Then)")
    void givenBeanCreated_whenContextRefresh_thenPostConstructCalled() {
        // Given: ApplicationContext 생성
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AnnotationClass.class);
        // When: 컨텍스트를 refresh하여 Bean 생성
        context.refresh();
        // Then: 로그를 통해 @PostConstruct 호출 확인 (Slf4j 로그)
        context.close();
    }
}