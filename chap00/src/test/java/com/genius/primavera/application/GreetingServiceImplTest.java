package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingServiceImplTest {

    @Test
    @DisplayName("hello() 메서드는 'Hello'를 반환해야 한다")
    void helloTest() {
        // given
        GreetingService greetingService = new GreetingServiceImpl();
        // when
        String result = greetingService.hello();
        // then
        assertThat(result).isEqualTo("Hello");
    }
}