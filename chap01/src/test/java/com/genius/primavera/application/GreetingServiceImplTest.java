package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingServiceImplTest {

    /**
     * GreetingServiceImplTest - GreetingServiceImpl 클래스의 테스트 클래스입니다.
     * 이 클래스는 GreetingServiceImpl의 hello() 메서드가 올바르게 동작하는지 검증합니다.
     */
    @Test
    @DisplayName("hello() 메서드는 'Hello'를 반환해야 한다")
    void helloTest() {
        HelloService greetingService = new HelloServiceImpl();
        String result = greetingService.hello();
        assertThat(result).isEqualTo("Hello");
    }
}