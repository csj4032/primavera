package com.genius.primavera.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Slf4j
@Configuration
public class LoggingConfiguration {

    private final Environment environment;

    public LoggingConfiguration(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        String logPath = environment.getProperty("primavera.logs.path", "./logs");
        log.info("translated_text_7 translated_text_7. translated_text_2 translated_text_4: {}", Arrays.toString(activeProfiles));
        log.info("translated_text_2 translated_text_2 translated_text_2: {}", logPath);
    }
}