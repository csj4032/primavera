package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorldServiceImpl Test")
public class WorldServiceImplTest {

    @Test
    @DisplayName("hello() file 'Hello'should file test")
    public void hello() {
        WorldService worldService = new WorldServiceImpl();
        String result = worldService.hello();
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("world() file 'World'should file test")
    public void world() {
        WorldService worldService = new WorldServiceImpl();
        String result = worldService.world();
        assertThat(result).isEqualTo("World");
    }

    @Test
    @DisplayName("hello()should world() file test 'Hello'should 'World'should file test")
    public void helloAndWorld() {
        WorldService worldService = new WorldServiceImpl();
        String hello = worldService.hello();
        String world = worldService.world();
        assertThat(hello).isEqualTo("Hello");
        assertThat(world).isEqualTo("World");
    }
}