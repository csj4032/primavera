package com.genius.primavera.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@DisplayName("AnnotationClass with test")
class AnnotationClassTest {

    @Test
    @DisplayName("Bean creation should @PostConstructshould successfully calledtest (Given-When-Then)")
    void givenBeanCreated_whenContextRefresh_thenPostConstructCalled() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AnnotationClass.class);
        context.refresh();
        context.close();
    }
}