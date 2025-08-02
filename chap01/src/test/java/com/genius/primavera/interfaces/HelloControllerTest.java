package com.genius.primavera.interfaces;

import com.genius.primavera.application.HelloService;
import com.genius.primavera.application.WorldService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelloControllerTest {

    @Mock
    private HelloService greetingService;

    @Mock
    private WorldService worldService;

    @InjectMocks
    private HelloController helloController;

    @Test
    @DisplayName("greeting() 메서드는 'Hello World!!!'를 반환해야 한다")
    void greetingTest() {
        when(greetingService.hello()).thenReturn("Hello");
        when(worldService.world()).thenReturn("World");
        String result = helloController.hello();
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("hello() 메서드는 GreetingService의 hello() 메서드 결과를 반환해야 한다")
    void helloTest() {
        when(greetingService.hello()).thenReturn("Hello");
        String result = helloController.hello();
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("world() 메서드는 WorldService의 world() 메서드 결과를 반환해야 한다")
    void worldTest() {
        when(worldService.world()).thenReturn("World");
        String result = helloController.world();
        assertThat(result).isEqualTo("World");
    }
}