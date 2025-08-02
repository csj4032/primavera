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
class WorldControllerTest {

    @Mock
    private HelloService helloService;

    @Mock
    private WorldService worldService;

    @Test
    @DisplayName("world() 메서드는 'World!!! Hello'를 반환해야 한다")
    void worldTest() {
        // given
        when(worldService.world()).thenReturn("World!!!");
        when(helloService.hello()).thenReturn("Hello");
        
        // WorldController는 record이므로 직접 생성
        WorldController worldController = new WorldController(helloService, worldService);

        // when
        String result = worldController.world();

        // then
        assertThat(result).isEqualTo("World!!! Hello");
    }

    @Test
    @DisplayName("WorldController가 정상적으로 생성되어야 한다")
    void constructorTest() {
        // when
        WorldController worldController = new WorldController(helloService, worldService);

        // then
        assertThat(worldController).isNotNull();
        assertThat(worldController.helloService()).isEqualTo(helloService);
        assertThat(worldController.worldService()).isEqualTo(worldService);
    }
}