package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingServiceImplTest {

    @Test
    @DisplayName("hello() translated_text_4 'Hello'translated_text_1 translated_text_4 translated_text_2")
    void helloTest() {
        HelloService greetingService = new HelloServiceImpl();
        String result = greetingService.hello();
        assertThat(result).isEqualTo("Hello");
    }
}