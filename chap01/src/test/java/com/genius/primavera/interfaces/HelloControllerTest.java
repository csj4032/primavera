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
public class HelloControllerTest {

    @Mock
    private HelloService greetingService;

    @Mock
    private WorldService worldService;

    @InjectMocks
    private HelloController helloController;

    @Test
    @DisplayName("greeting() file 'Hello World!!!'should file test")
    void greetingTest() {
        when(greetingService.hello()).thenReturn("Hello");
        when(worldService.world()).thenReturn("World");
        String result = helloController.hello();
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("hello() file GreetingServiceshould hello() connection should file test")
    void helloTest() {
        when(greetingService.hello()).thenReturn("Hello");
        when(worldService.world()).thenReturn("World");
        String result = helloController.hello();
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("world() file WorldServiceshould world() connection should file test")
    void worldTest() {
        when(worldService.world()).thenReturn("World");
        String result = helloController.world();
        assertThat(result).isEqualTo("World");
    }
}