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
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@SpringBootConfiguration
@EnableAutoConfiguration
public class SpringBootStarterApplication {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(SpringBootStarterApplication.class);

    public static void main(String[] args) {
        SpringApplication springApplication = (new SpringApplicationBuilder(new Class[]{SpringBootStarterApplication.class})).initializers((applicationContext) -> {
            log.info("!PrimaveraApplication initializers");
            if (applicationContext instanceof org.springframework.context.support.GenericApplicationContext) {
                org.springframework.context.support.GenericApplicationContext genericContext = (org.springframework.context.support.GenericApplicationContext) applicationContext;
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

    @Bean
    public WorldService worldService() {
        return new WorldServiceImpl();
    }

    @Bean
    public GreetingService greetingService() {
        return new GreetingServiceImpl();
    }

    @Bean
    public HelloController helloController(GreetingService greetingService, WorldService worldService) {
        return new HelloController(greetingService, worldService);
    }

    @EventListener({ApplicationStartingEvent.class})
    public void applicationStartingEvent(ApplicationStartingEvent applicationStartingEvent) {
        System.out.println("! PrimaveraApplication : " + applicationStartingEvent.toString());
    }

    @EventListener({ServletWebServerInitializedEvent.class})
    public void applicationStartingEvent(ServletWebServerInitializedEvent servletWebServerInitializedEvent) {
        log.info("! PrimaveraApplication : {}", servletWebServerInitializedEvent.toString());
    }

    @EventListener({ApplicationContextInitializedEvent.class})
    public void applicationContextInitializedEvent(ApplicationContextInitializedEvent applicationContextInitializedEvent) {
        log.info("! PrimaveraApplication : {}", applicationContextInitializedEvent.toString());
    }

    @PostConstruct
    private void postConstruct() {
        log.info("! PrimaveraApplication : postConstruct");
    }

    @EventListener({ApplicationStartedEvent.class})
    public void applicationStartedEvent(ApplicationStartedEvent applicationStartedEvent) {
        log.info("! PrimaveraApplication : {}", applicationStartedEvent.toString());
    }

    @EventListener({ApplicationReadyEvent.class})
    public void applicationReadyEvent(ApplicationReadyEvent applicationReadyEvent) {
        log.info("! PrimaveraApplication : {}", applicationReadyEvent.toString());
    }

    @Bean
    protected ApplicationRunner applicationRunner() {
        return (args) -> log.info("!! PrimaveraApplicationRunner Runner Args: {}", (Object) args);
    }

    @Bean
    protected CommandLineRunner commandLineRunner() {
        return (args) -> log.info("!!! PrimaveraApplicationRunner Runner Args: {}", (Object) args);
    }
}
