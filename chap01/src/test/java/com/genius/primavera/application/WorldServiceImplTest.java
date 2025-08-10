package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorldServiceImpl Test")
public class WorldServiceImplTest {

    @Test
    @DisplayName("hello() translated_text_4 'Hello'translated_text_1 translated_text_4 translated_text_2")
    public void hello() {
        WorldService worldService = new WorldServiceImpl();
        String result = worldService.hello();
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("world() translated_text_4 'World'translated_text_1 translated_text_4 translated_text_2")
    public void world() {
        WorldService worldService = new WorldServiceImpl();
        String result = worldService.world();
        assertThat(result).isEqualTo("World");
    }

    @Test
    @DisplayName("hello()translated_text_1 world() translated_text_4 translated_text_2 'Hello'translated_text_1 'World'translated_text_1 translated_text_4 translated_text_2")
    public void helloAndWorld() {
        WorldService worldService = new WorldServiceImpl();
        String hello = worldService.hello();
        String world = worldService.world();
        assertThat(hello).isEqualTo("Hello");
        assertThat(world).isEqualTo("World");
    }
}