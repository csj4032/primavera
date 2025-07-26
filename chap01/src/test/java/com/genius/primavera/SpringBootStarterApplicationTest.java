package com.genius.primavera;

import com.genius.primavera.application.WorldService;
import com.genius.primavera.application.GreetingService;
import com.genius.primavera.interfaces.HelloController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpringBootStarterApplication.class)
@RecordApplicationEvents
public class SpringBootStarterApplicationTest {


    @Autowired
    private ApplicationContext context;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WorldService worldService() {
            return new WorldService() {
                @Override
                public String world() {
                    return "World!!!";
                }

                @Override
                public String hello() {
                    return "Hello!!!";
                }
            };
        }

        @Bean
        public GreetingService greetingService() {
            return () -> "Hello";
        }

        @Bean
        public HelloController helloController(WorldService worldService, GreetingService greetingService) {
            return new HelloController(greetingService, worldService);
        }
    }


    @Test
    @DisplayName("WorldService 빈이 정상적으로 등록되고 hello()가 'World!!!'를 반환한다.")
    void worldServiceBeanIsRegistered() {
        // given
        WorldService worldService = context.getBean(WorldService.class);
        // when
        String helloResult = worldService.hello();
        // then
        assertThat(worldService).isNotNull();
        assertThat(helloResult).isEqualTo("World!!!");
    }

    @Test
    @DisplayName("HelloController 빈이 정상적으로 등록되고 hello(), world()가 각각 'Hello', 'World!!!'를 반환한다.")
    void helloControllerBeanIsRegistered() {
        // given
        HelloController helloController = context.getBean(HelloController.class);
        // when
        String helloResult = helloController.hello();
        String worldResult = helloController.world();
        // then
        assertThat(helloController).isNotNull();
        assertThat(helloResult).isEqualTo("Hello");
        assertThat(worldResult).isEqualTo("World!!!");
    }

    @Test
    @DisplayName("ApplicationContext가 정상적으로 생성되고 WorldService, HelloController 빈이 등록되어 있다.")
    void applicationContextEventsArePublished() {
        assertThat(context).isNotNull();
        assertThat(context.getBean(WorldService.class)).isNotNull();
        assertThat(context.getBean(HelloController.class)).isNotNull();
    }

    @Test
    @DisplayName("WorldService의 world()가 'World!!!'를 반환한다.")
    void worldServiceWorldMethodReturnsExpectedValue() {
        // given
        WorldService worldService = context.getBean(WorldService.class);
        // when
        String worldResult = worldService.world();
        // then
        assertThat(worldResult).isEqualTo("World!!!");
    }

    @Test
    @DisplayName("HelloController가 생성자 주입으로 WorldService와 GreetingService를 정상적으로 받는다.")
    void helloControllerConstructorInjection() {
        // given
        HelloController helloController = context.getBean(HelloController.class);
        // when & then
        assertThat(helloController).isNotNull();
    }

    @Test
    @DisplayName("ApplicationContext에 GreetingService 빈이 정상적으로 등록되어 있다.")
    void greetingServiceBeanIsRegistered() {
        // given
        Object greetingService = context.getBean("greetingService");
        // then
        assertThat(greetingService).isNotNull();
    }
}
