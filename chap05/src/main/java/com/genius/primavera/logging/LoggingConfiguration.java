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
        log.info("애플리케이션이 시작되었습니다. 활성 프로파일: {}", Arrays.toString(activeProfiles));
        log.info("로그 파일 경로: {}", logPath);
    }
}