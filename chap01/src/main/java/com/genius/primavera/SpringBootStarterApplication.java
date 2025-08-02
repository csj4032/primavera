package com.genius.primavera;

import com.genius.primavera.application.HelloService;
import com.genius.primavera.application.HelloServiceImpl;
import com.genius.primavera.application.WorldService;
import com.genius.primavera.application.WorldServiceImpl;
import com.genius.primavera.interfaces.WorldController;
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
 * 
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 01-05: Spring Boot 기초)
 * =============================================================================
 * 
 * 1️⃣ 인프라 시작:
 *    cd infrastructure
 *    docker-compose -f docker-compose.basic.yml up -d
 * 
 * 2️⃣ 상태 확인:
 *    docker-compose -f docker-compose.basic.yml ps
 * 
 * 3️⃣ 애플리케이션 실행:
 *    ./gradlew :chap01:bootRun -Dspring.profiles.active=local
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308
 *    - 데이터베이스: primavera, primavera_basic
 *    - 접속 정보: primavera/primavera
 * 
 * =============================================================================
 */
@ComponentScan
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class SpringBootStarterApplication {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(SpringBootStarterApplication.class);

    public static void main(String[] args) {
        SpringApplication springApplication = (new SpringApplicationBuilder(SpringBootStarterApplication.class)).initializers((applicationContext) -> {
            log.info("[SpringBoot] SpringBootStarterApplication initializers");
            if (applicationContext instanceof org.springframework.context.support.GenericApplicationContext genericContext) {
                // 컴포넌트 스캔으로 이미 등록된 구현체들을 사용하여 WorldController를 등록
                genericContext.registerBean("worldController", WorldController.class, () -> {
                    // 컴포넌트 스캔으로 등록된 구현체들을 가져와서 사용
                    WorldService worldService = genericContext.getBean("worldServiceImpl", WorldService.class);
                    HelloService helloService = genericContext.getBean("helloServiceImpl", HelloService.class);
                    log.info("[SpringBoot] WorldController 동적 등록 - WorldService: {}, HelloService: {}", worldService.getClass().getSimpleName(), helloService.getClass().getSimpleName());
                    return new WorldController(helloService, worldService);
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