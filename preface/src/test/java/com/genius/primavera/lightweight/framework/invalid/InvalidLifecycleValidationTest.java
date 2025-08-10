package com.genius.primavera.lightweight.framework.invalid;

import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.annotations.PrimaveraPostConstruct;
import com.genius.primavera.lightweight.framework.PrimaveraApplicationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InvalidLifecycleValidationTest {

    @Test
    @DisplayName("translated_text_3 @PostConstruct translated_text_3 (translated_text_4 translated_text_2)translated_text_1 translated_text_10 translated_text_6 test")
    void shouldThrowExceptionForInvalidPostConstructMethod() {
        assertThrows(RuntimeException.class, () -> {
            new PrimaveraApplicationContext("com.genius.primavera.lightweight.framework.invalid");
        });
    }

    @PrimaveraComponent
    public static class InvalidLifecycleBean {

        @PrimaveraPostConstruct
        public void invalidInit(String parameter) {

        }
    }
}