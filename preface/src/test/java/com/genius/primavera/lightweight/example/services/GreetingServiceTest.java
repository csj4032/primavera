package com.genius.primavera.lightweight.example.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GreetingService 테스트
 */
class GreetingServiceTest {
    
    private GreetingService greetingService;
    
    @BeforeEach
    void setUp() {
        greetingService = new GreetingService();
    }
    
    @Test
    @DisplayName("기본 인사말이 올바르게 생성되는지 테스트")
    void shouldSayHello() {
        String result = greetingService.sayHello("테스터");
        
        assertNotNull(result);
        assertTrue(result.contains("테스터"));
        assertTrue(result.contains("환영합니다"));
    }
    
    @Test
    @DisplayName("시간이 포함된 인사말이 올바르게 생성되는지 테스트")
    void shouldSayHelloWithTime() {
        String result = greetingService.sayHelloWithTime("테스터");
        
        assertNotNull(result);
        assertTrue(result.contains("테스터"));
        assertTrue(result.contains("현재 시간"));
    }
    
    @Test
    @DisplayName("작별 인사가 올바르게 생성되는지 테스트")
    void shouldSayGoodbye() {
        String result = greetingService.sayGoodbye("테스터");
        
        assertNotNull(result);
        assertTrue(result.contains("테스터"));
        assertTrue(result.contains("안녕히 가세요"));
    }
    
    @Test
    @DisplayName("null 이름으로 인사말 생성 시 예외 처리 테스트")
    void shouldHandleNullName() {
        // null 이름도 처리할 수 있어야 함
        assertDoesNotThrow(() -> {
            greetingService.sayHello(null);
        });
    }
}