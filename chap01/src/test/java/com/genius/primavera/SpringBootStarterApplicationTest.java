package com.genius.primavera;

import com.genius.primavera.application.HelloService;
import com.genius.primavera.application.WorldService;
import com.genius.primavera.interfaces.HelloController;
import com.genius.primavera.interfaces.WorldController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
public class SpringBootStarterApplicationTest {

    @Autowired
    private ApplicationContext context;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WorldController worldController(@Autowired ApplicationContext context) {
            HelloService helloService = context.getBean("helloServiceImpl", HelloService.class);
            WorldService worldService = context.getBean("worldServiceImpl", WorldService.class);
            return new WorldController(helloService, worldService);
        }
    }

    @Test
    @DisplayName("WorldService 빈이 정상적으로 등록되고 hello()가 'Hello'를 반환한다.")
    void worldServiceBeanIsRegistered() {
        WorldService worldService = context.getBean(WorldService.class);
        String helloResult = worldService.hello();
        assertThat(worldService).isNotNull();
        assertThat(helloResult).isEqualTo("Hello");
    }

    @Test
    @DisplayName("HelloController 빈이 정상적으로 등록되고 hello(), world()가 각각 'Hello World!!!', 'World!!!'를 반환한다.")
    void helloControllerBeanIsRegistered() {
        HelloController helloController = context.getBean(HelloController.class);
        String helloResult = helloController.hello();
        String worldResult = helloController.world();
        assertThat(helloController).isNotNull();
        assertThat(helloResult).isEqualTo("Hello World");
        assertThat(worldResult).isEqualTo("World");
    }

    @Test
    @DisplayName("ApplicationContext가 정상적으로 생성되고 모든 필요한 빈이 등록되어 있다.")
    void applicationContextEventsArePublished() {
        assertThat(context).isNotNull();
        assertThat(context.getBean(WorldService.class)).isNotNull();
        assertThat(context.getBean(HelloController.class)).isNotNull();
        assertThat(context.getBean("helloServiceImpl")).isNotNull();
        assertThat(context.getBean(WorldController.class)).isNotNull();
        assertThat(context.getBean(HelloService.class)).isNotNull();
    }

    @Test
    @DisplayName("WorldService의 world()가 'World!!!'를 반환한다.")
    void worldServiceWorldMethodReturnsExpectedValue() {
        WorldService worldService = context.getBean(WorldService.class);
        String worldResult = worldService.world();
        assertThat(worldResult).isEqualTo("World");
    }

    @Test
    @DisplayName("HelloController가 어노테이션 기반으로 정상적으로 등록된다.")
    void helloControllerConstructorInjection() {
        HelloController helloController = context.getBean(HelloController.class);
        assertThat(helloController).isNotNull();
    }

    @Test
    @DisplayName("WorldController가 프로그래매틱 방식으로 정상적으로 등록되고 의존성 주입이 동작한다.")
    void worldControllerConstructorInjection() {
        WorldController worldController = context.getBean(WorldController.class);
        assertThat(worldController).isNotNull();
        String worldResult = worldController.world();
        assertThat(worldResult).isEqualTo("World Hello");
    }

    @Test
    @DisplayName("ApplicationContext에 HelloService 빈이 정상적으로 등록되어 있다.")
    void helloServiceBeanIsRegistered() {
        HelloService helloService = context.getBean(HelloService.class);
        assertThat(helloService).isNotNull();
        assertThat(helloService.hello()).isEqualTo("Hello");
    }
}
