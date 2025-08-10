package com.genius.primavera.lightweight.example.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GreetingServiceTest {
    
    private GreetingService greetingService;
    
    @BeforeEach
    void setUp() {
        greetingService = new GreetingService();
    }
    
    @Test
    @DisplayName("test file processing test")
    void shouldSayHello() {
        String result = greetingService.sayHello("connection");
        
        assertNotNull(result);
        assertTrue(result.contains("connection"));
        assertTrue(result.contains("Endpoint"));
    }
    
    @Test
    @DisplayName("connection file processing test")
    void shouldSayHelloWithTime() {
        String result = greetingService.sayHelloWithTime("connection");
        
        assertNotNull(result);
        assertTrue(result.contains("connection"));
        assertTrue(result.contains("test"));
    }
    
    @Test
    @DisplayName("test connection file processing test")
    void shouldSayGoodbye() {
        String result = greetingService.sayGoodbye("connection");
        
        assertNotNull(result);
        assertTrue(result.contains("connection"));
        assertTrue(result.contains("connection"));
    }
    
    @Test
    @DisplayName("null file connection creation should exception processing test")
    void shouldHandleNullName() {

        assertDoesNotThrow(() -> {
            greetingService.sayHello(null);
        });
    }
}