package com.genius.primavera;

import com.genius.primavera.application.WorldService;
import com.genius.primavera.interfaces.HelloController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootConfiguration
@EnableAutoConfiguration
public class PrimaveraApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplicationBuilder(PrimaveraApplication.class)
                .initializers((GenericApplicationContext applicationContext) -> {
                    log.info("! PrimaveraApplication initializers");
                    applicationContext.registerBean("world", WorldService.class, () -> () -> "World!!!");
                    applicationContext.registerBean(HelloController.class, () -> new HelloController(() -> "Hello", applicationContext.getBean("world", WorldService.class)));
                })
                .logStartupInfo(true)
                .build();
        springApplication.setLazyInitialization(true);
        springApplication.run(args);
    }

    @EventListener(ApplicationStartingEvent.class)
    public void applicationStartingEvent(ApplicationStartingEvent applicationStartingEvent) {
        System.out.println("! PrimaveraApplication : " + applicationStartingEvent.toString());
    }

    @EventListener(ServletWebServerInitializedEvent.class)
    public void applicationStartingEvent(ServletWebServerInitializedEvent servletWebServerInitializedEvent) {
        log.info("! PrimaveraApplication : {}", servletWebServerInitializedEvent.toString());
    }

    @EventListener(ApplicationContextInitializedEvent.class)
    public void applicationContextInitializedEvent(ApplicationContextInitializedEvent applicationContextInitializedEvent) {
        log.info("! PrimaveraApplication : {}", applicationContextInitializedEvent.toString());
    }

    @PostConstruct
    private void postConstruct() {
        log.info("! PrimaveraApplication : postConstruct");
    }

    @EventListener(ApplicationStartedEvent.class)
    public void applicationStartedEvent(ApplicationStartedEvent applicationStartedEvent) {
        log.info("! PrimaveraApplication : {}", applicationStartedEvent.toString());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReadyEvent(ApplicationReadyEvent applicationReadyEvent) {
        log.info("! PrimaveraApplication : {}", applicationReadyEvent.toString());
    }

    @Bean
    protected ApplicationRunner applicationRunner() {
        return (args) -> log.info("!! PrimaveraApplicationRunner Runner Args: {}", args);
    }

    @Bean
    protected CommandLineRunner commandLineRunner() {
        return (args) -> log.info("!!! PrimaveraApplicationRunner Runner Args: {}", args);
    }
}

@Slf4j
//@Component
class PrimaveraApplicationRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("! PrimaveraApplicationRunner Runner Args: {}", args);
    }
}

@Slf4j
//@Component
class PrimaveraCommandLineRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        log.info("! PrimaveraCommandLineRunner Runner Args: {}", args);
    }
}
