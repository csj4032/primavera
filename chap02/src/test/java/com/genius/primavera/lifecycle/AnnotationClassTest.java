package com.genius.primavera.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@DisplayName("AnnotationClass translated_text_6 test")
class AnnotationClassTest {

    @Test
    @DisplayName("Bean creation translated_text_1 @PostConstructtranslated_text_1 successfully calledtranslated_text_2 (Given-When-Then)")
    void givenBeanCreated_whenContextRefresh_thenPostConstructCalled() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AnnotationClass.class);
        context.refresh();
        context.close();
    }
}