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
    @DisplayName("WorldService bean is successfully registered and hello()translated_text_1 'Hello'translated_text_1 returns.")
    void worldServiceBeanIsRegistered() {
        WorldService worldService = context.getBean(WorldService.class);
        String helloResult = worldService.hello();
        assertThat(worldService).isNotNull();
        assertThat(helloResult).isEqualTo("Hello");
    }

    @Test
    @DisplayName("HelloController bean is successfully registered and hello(), world()translated_text_1 translated_text_2 'Hello World!!!', 'World!!!'translated_text_1 returns.")
    void helloControllerBeanIsRegistered() {
        HelloController helloController = context.getBean(HelloController.class);
        String helloResult = helloController.hello();
        String worldResult = helloController.world();
        assertThat(helloController).isNotNull();
        assertThat(helloResult).isEqualTo("Hello World");
        assertThat(worldResult).isEqualTo("World");
    }

    @Test
    @DisplayName("ApplicationContext is successfully created and all necessary bean is registered exists.")
    void applicationContextEventsArePublished() {
        assertThat(context).isNotNull();
        assertThat(context.getBean(WorldService.class)).isNotNull();
        assertThat(context.getBean(HelloController.class)).isNotNull();
        assertThat(context.getBean("helloServiceImpl")).isNotNull();
        assertThat(context.getBean(WorldController.class)).isNotNull();
        assertThat(context.getBean(HelloService.class)).isNotNull();
    }

    @Test
    @DisplayName("WorldService's world() method 'World!!!'translated_text_1 returns.")
    void worldServiceWorldMethodReturnsExpectedValue() {
        WorldService worldService = context.getBean(WorldService.class);
        String worldResult = worldService.world();
        assertThat(worldResult).isEqualTo("World");
    }

    @Test
    @DisplayName("HelloControllertranslated_text_1 annotation based successfully translated_text_14.")
    void helloControllerConstructorInjection() {
        HelloController helloController = context.getBean(HelloController.class);
        assertThat(helloController).isNotNull();
    }

    @Test
    @DisplayName("WorldControllertranslated_text_1 programmatic method successfully registered and dependency injection works.")
    void worldControllerConstructorInjection() {
        WorldController worldController = context.getBean(WorldController.class);
        assertThat(worldController).isNotNull();
        String worldResult = worldController.world();
        assertThat(worldResult).isEqualTo("World Hello");
    }

    @Test
    @DisplayName("ApplicationContext has HelloService bean is successfully registered exists.")
    void helloServiceBeanIsRegistered() {
        HelloService helloService = context.getBean(HelloService.class);
        assertThat(helloService).isNotNull();
        assertThat(helloService.hello()).isEqualTo("Hello");
    }
}
