package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingServiceImplTest {

    @Test
    @DisplayName("hello() file 'Hello'should file test")
    void helloTest() {
        HelloService greetingService = new HelloServiceImpl();
        String result = greetingService.hello();
        assertThat(result).isEqualTo("Hello");
    }
}