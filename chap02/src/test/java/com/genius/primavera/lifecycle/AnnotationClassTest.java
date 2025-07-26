package com.genius.primavera.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@DisplayName("AnnotationClass 라이프사이클 테스트")
class AnnotationClassTest {

    @Test
    @DisplayName("Bean 생성 시 @PostConstruct가 정상적으로 호출된다 (Given-When-Then)")
    void givenBeanCreated_whenContextRefresh_thenPostConstructCalled() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AnnotationClass.class);
        context.refresh();
        context.close();
    }
}