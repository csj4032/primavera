package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HelloService Test")
public class HelloServiceTest {

    @Test
    @DisplayName("Test HelloService hello method")
    public void testHello() {
        HelloService helloService = () -> "Hello, World!";
        String result = helloService.hello();
        assertEquals("Hello, World!", result);
    }

    @Test
    @DisplayName("Test HelloService hello method with different message")
    public void testHelloWithDifferentMessage() {
        HelloService helloService = () -> "Hello, Primavera!";
        String result = helloService.hello();
        assertEquals("Hello, Primavera!", result);
    }
}