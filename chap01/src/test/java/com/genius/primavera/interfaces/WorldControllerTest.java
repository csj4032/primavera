package com.genius.primavera.interfaces;

import com.genius.primavera.application.HelloService;
import com.genius.primavera.application.WorldService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WorldControllerTest {

    @Mock
    private HelloService helloService;

    @Mock
    private WorldService worldService;

    @Test
    @DisplayName("world() file 'World!!! Hello'should file test")
    void worldTest() {
        when(worldService.world()).thenReturn("World!!!");
        when(helloService.hello()).thenReturn("Hello");
        WorldController worldController = new WorldController(helloService, worldService);
        String result = worldController.world();
        assertThat(result).isEqualTo("World!!! Hello");
    }

    @Test
    @DisplayName("WorldControllershould successfully processing test")
    void constructorTest() {
        WorldController worldController = new WorldController(helloService, worldService);
        assertThat(worldController).isNotNull();
        assertThat(worldController.helloService()).isEqualTo(helloService);
        assertThat(worldController.worldService()).isEqualTo(worldService);
    }
}