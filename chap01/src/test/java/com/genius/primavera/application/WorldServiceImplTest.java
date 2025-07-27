package com.genius.primavera.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * WorldServiceImplTest - WorldServiceImpl 클래스의 테스트 클래스입니다.
 * 이 클래스는 WorldServiceImpl의 world() 및 hello() 메서드가 올바르게 동작하는지 검증합니다.
 */
public class WorldServiceImplTest {

    /**
     * hello() 메서드는 'Hello'를 반환해야 한다
     * 이 테스트는 WorldServiceImpl 클래스의 hello() 메서드가 올바르게 동작하는지 검증합니다.
     */
    @Test
    @DisplayName("hello() 메서드는 'Hello'를 반환해야 한다")
    public void hello() {
        WorldService worldService = new WorldServiceImpl();
        String result = worldService.hello();
        assertThat(result).isEqualTo("Hello");
    }

    /**
     * world() 메서드는 'Wrold'를 반환해야 한다
     * 이 테스트는 WorldServiceImpl 클래스의 world() 메서드가 올바르게 동작하는지 검증합니다.
     */
    @Test
    @DisplayName("world() 메서드는 'World'를 반환해야 한다")
    public void world() {
        WorldService worldService = new WorldServiceImpl();
        String result = worldService.world();
        assertThat(result).isEqualTo("World");
    }

    /**
     * hello()와 world() 메서드는 각각 'Hello'와 'World'를 반환해야 한다
     * 이 테스트는 WorldServiceImpl 클래스의 hello() 및 world() 메서드가 올바르게 동작하는지 검증합니다.
     */
    @Test
    public void helloAndWorld() {
        WorldService worldService = new WorldServiceImpl();
        String hello = worldService.hello();
        String world = worldService.world();
        assertThat(hello).isEqualTo("Hello");
        assertThat(world).isEqualTo("World");
    }
}
