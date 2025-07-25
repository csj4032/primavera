package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WorldServiceImplTest {

    @Test
    @DisplayName("world() 메서드는 'World!!!'를 반환해야 한다")
    void world() {
        // given
        WorldService worldService = new WorldServiceImpl();

        // when
        String result = worldService.world();

        // then
        assertThat(result).isEqualTo("World!!!");
    }

    @Test
    @DisplayName("hello() 메서드는 'World!!!'를 반환해야 한다")
    void hello() {
        // given
        WorldService worldService = new WorldServiceImpl();

        // when
        String result = worldService.hello();

        // then
        assertThat(result).isEqualTo("World!!!");
    }
}