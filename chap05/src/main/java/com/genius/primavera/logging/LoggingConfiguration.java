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
        
        log.info("========================================");
        log.info("애플리케이션 로깅 설정 정보");
        log.info("========================================");
        log.info("활성 프로파일: {}", Arrays.toString(activeProfiles));
        log.info("로그 파일 경로: {}", logPath);
        
        // Profile Group 확장 결과 확인
        if (Arrays.asList(activeProfiles).contains("local")) {
            log.info("Local 프로파일 그룹 활성화:");
            log.info("  - console-appender: 콘솔 출력");
            log.info("  - file-debug-appender: {}/debug/", logPath);
            log.info("  - file-info-appender: {}/info/", logPath);
            log.info("  - file-warn-appender: {}/warn/", logPath);
            log.info("  - file-error-appender: {}/error/", logPath);
        } else if (Arrays.asList(activeProfiles).contains("test")) {
            log.info("Test 프로파일 그룹 활성화:");
            log.info("  - console-appender: 콘솔 출력만 활성화");
        }
        
        log.info("========================================");
        
        // 각 로거의 레벨 정보 출력
        log.debug("com.genius.primavera 패키지 로그 레벨: DEBUG");
        log.debug("ROOT 로거 레벨: DEBUG");
    }
}