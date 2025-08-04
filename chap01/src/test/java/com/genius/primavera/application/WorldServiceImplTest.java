package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorldServiceImpl Test")
public class WorldServiceImplTest {

    @Test
    @DisplayName("hello() 메서드는 'Hello'를 반환해야 한다")
    public void hello() {
        WorldService worldService = new WorldServiceImpl();
        String result = worldService.hello();
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("world() 메서드는 'World'를 반환해야 한다")
    public void world() {
        WorldService worldService = new WorldServiceImpl();
        String result = worldService.world();
        assertThat(result).isEqualTo("World");
    }

    @Test
    @DisplayName("hello()와 world() 메서드는 각각 'Hello'와 'World'를 반환해야 한다")
    public void helloAndWorld() {
        WorldService worldService = new WorldServiceImpl();
        String hello = worldService.hello();
        String world = worldService.world();
        assertThat(hello).isEqualTo("Hello");
        assertThat(world).isEqualTo("World");
    }
}