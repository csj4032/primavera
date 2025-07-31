package com.genius.primavera;

import com.genius.primavera.application.GreetingService;
import com.genius.primavera.application.GreetingServiceImpl;
import com.genius.primavera.application.WorldService;
import com.genius.primavera.application.WorldServiceImpl;
import com.genius.primavera.interfaces.HelloController;
import jakarta.annotation.PostConstruct;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

/**
 * Spring Boot 애플리케이션의 시작점입니다.
 * 이 클래스는 Spring Boot의 자동 설정, 컴포넌트 스캔 및 애플리케이션 초기화를 담당합니다.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
@EnableConfigurationProperties
public class SpringBootStarterApplication {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(SpringBootStarterApplication.class);

    public static void main(String[] args) {
        SpringApplication springApplication = (new SpringApplicationBuilder(new Class[]{SpringBootStarterApplication.class})).initializers((applicationContext) -> {
            log.info("[SpringBoot] PrimaveraApplication initializers");
            // 프로그래매틱 Bean 등록 예제 (학습용)
            // HelloController는 @RestController로도 등록되므로 Bean 중복 충돌이 발생할 수 있습니다.
            // 실제 개발 시에는 @Component 어노테이션 방식 또는 프로그래매틱 방식 중 하나만 사용하세요.
            // 충돌 발생 시 아래 HelloController 등록 부분을 주석 처리하거나
            // HelloController에서 @RestController 어노테이션을 제거하세요.
            if (applicationContext instanceof org.springframework.context.support.GenericApplicationContext genericContext) {
                genericContext.registerBean(WorldService.class, WorldServiceImpl.class);
                genericContext.registerBean(GreetingService.class, GreetingServiceImpl.class);
                genericContext.registerBean(HelloController.class, () -> {
                    WorldService worldService = genericContext.getBean(WorldService.class);
                    GreetingService greetingService = genericContext.getBean(GreetingService.class);
                    return new HelloController(greetingService, worldService);
                });
            }
        }).logStartupInfo(true).build();
        springApplication.setLazyInitialization(true);
        springApplication.run(args);
    }

    /**
     * Spring Boot 애플리케이션의 시작 이벤트를 처리합니다.
     * 이 메서드는 애플리케이션이 시작될 때 호출됩니다.
     */
    @EventListener({ApplicationStartingEvent.class})
    public void applicationStartingEvent(ApplicationStartingEvent applicationStartingEvent) {
        log.info("[SpringBoot] ApplicationStartingEvent: {}", applicationStartingEvent);
    }

    /**
     * ServletWebServerInitializedEvent 이벤트를 처리합니다.
     * 이 메서드는 서블릿 웹 서버가 초기화될 때 호출됩니다.
     */
    @EventListener({ServletWebServerInitializedEvent.class})
    public void servletWebServerInitializedEvent(ServletWebServerInitializedEvent event) {
        log.info("[SpringBoot] ServletWebServerInitializedEvent: {}", event);
    }

    /**
     * ApplicationContextInitializedEvent 이벤트를 처리합니다.
     * 이 메서드는 애플리케이션 컨텍스트가 초기화될 때 호출됩니다.
     */
    @EventListener({ApplicationContextInitializedEvent.class})
    public void applicationContextInitializedEvent(ApplicationContextInitializedEvent event) {
        log.info("[SpringBoot] ApplicationContextInitializedEvent: {}", event);
    }

    /**
     * @PostConstruct 메서드입니다.
     * 이 메서드는 Spring Bean이 초기화된 후 호출됩니다.
     */
    @PostConstruct
    private void postConstruct() {
        log.info("[SpringBoot] @PostConstruct 호출");
    }

    /**
     * ApplicationStartedEvent 이벤트를 처리합니다.
     * 이 메서드는 애플리케이션이 시작된 후 호출됩니다.
     */
    @EventListener({ApplicationStartedEvent.class})
    public void applicationStartedEvent(ApplicationStartedEvent event) {
        log.info("[SpringBoot] ApplicationStartedEvent: {}", event);
    }

    /**
     * ApplicationReadyEvent 이벤트를 처리합니다.
     * 이 메서드는 애플리케이션이 준비되었을 때 호출됩니다.
     */
    @EventListener({ApplicationReadyEvent.class})
    public void applicationReadyEvent(ApplicationReadyEvent event) {
        log.info("[SpringBoot] ApplicationReadyEvent: {}", event);
    }

    /**
     * ApplicationRunner Bean을 등록합니다.
     * 이 메서드는 애플리케이션이 시작된 후 실행됩니다.
     */
    @Bean
    protected ApplicationRunner applicationRunner() {
        return (args) -> log.info("[SpringBoot] ApplicationRunner Args: {}", (Object) args);
    }

    /**
     * CommandLineRunner Bean을 등록합니다.
     * 이 메서드는 애플리케이션이 시작된 후 실행됩니다.
     */
    @Bean
    protected CommandLineRunner commandLineRunner() {
        return (args) -> log.info("[SpringBoot] CommandLineRunner Args: {}", (Object) args);
    }
}
