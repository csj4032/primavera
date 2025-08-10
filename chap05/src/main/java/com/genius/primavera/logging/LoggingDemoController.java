package com.genius.primavera.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/logging")
public class LoggingDemoController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${primavera.logs.path:./logs}")
    private String logPath;

    @GetMapping("/demo")
    public Map<String, Object> demonstrateLogging() {
        log.trace("TRACE test - test connection information");
        log.debug("DEBUG test - connection information");
        log.info("INFO test - test information connection");
        log.warn("WARN test - test connection");
        log.error("ERROR test - test connection");
        String user = "user";
        LocalDateTime now = LocalDateTime.now();
        log.info("user [{}]should {}should test connection created successfully", user, now);
        try {
            throw new RuntimeException("file exception test");
        } catch (Exception e) {
            log.error("exceptionshould test", e);
        }
        return Map.of("activeProfile", activeProfile, "logPath", logPath, "timestamp", now, "message", "test connection should5: " + logPath);
    }

    @GetMapping("/profile-info")
    public Map<String, Object> getProfileInfo() {
        log.info("test file: {}", activeProfile);
        log.info("test: {}", logPath);
        if ("local".equals(activeProfile)) {
            log.debug("test should execution should - test connection information test");
            log.debug("test test appendershould test");
        } else if ("test".equals(activeProfile)) {
            log.info("test should execution should - test test");
        }
        return Map.of("profile", activeProfile, "logPath", logPath, "fileLoggingEnabled", "local".equals(activeProfile));
    }
}