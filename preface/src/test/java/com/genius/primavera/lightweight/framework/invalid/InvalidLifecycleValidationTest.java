package com.genius.primavera.lightweight.framework.invalid;

import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.annotations.PrimaveraPostConstruct;
import com.genius.primavera.lightweight.framework.PrimaveraApplicationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 잘못된 라이프사이클 어노테이션 사용에 대한 검증 테스트
 */
class InvalidLifecycleValidationTest {

    @Test
    @DisplayName("잘못된 @PostConstruct 메서드 (매개변수 있음)가 예외를 발생시키는지 테스트")
    void shouldThrowExceptionForInvalidPostConstructMethod() {
        assertThrows(RuntimeException.class, () -> {
            new PrimaveraApplicationContext("com.genius.primavera.lightweight.framework.invalid");
        });
    }

    // 잘못된 라이프사이클 Bean - 매개변수가 있는 @PostConstruct 메서드
    @PrimaveraComponent
    public static class InvalidLifecycleBean {
        
        // 잘못된 @PostConstruct 메서드 (매개변수 있음)
        @PrimaveraPostConstruct
        public void invalidInit(String parameter) {
            // 이 메서드는 예외를 발생시켜야 함
        }
    }
}