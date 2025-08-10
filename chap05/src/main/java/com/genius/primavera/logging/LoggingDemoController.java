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
        log.trace("TRACE translated_text_2 - translated_text_2 translated_text_3 information");
        log.debug("DEBUG translated_text_2 - translated_text_3 information");
        log.info("INFO translated_text_2 - translated_text_2 information translated_text_3");
        log.warn("WARN translated_text_2 - translated_text_2 translated_text_3");
        log.error("ERROR translated_text_2 - translated_text_2 translated_text_3");
        String user = "user";
        LocalDateTime now = LocalDateTime.now();
        log.info("user [{}]translated_text_1 {}translated_text_1 translated_text_2 translated_text_3 translated_text_13", user, now);
        try {
            throw new RuntimeException("translated_text_4 exception translated_text_2");
        } catch (Exception e) {
            log.error("exceptiontranslated_text_1 translated_text_2", e);
        }
        return Map.of("activeProfile", activeProfile, "logPath", logPath, "timestamp", now, "message", "translated_text_2 translated_text_3 translated_text_15: " + logPath);
    }

    @GetMapping("/profile-info")
    public Map<String, Object> getProfileInfo() {
        log.info("translated_text_2 translated_text_2 translated_text_4: {}", activeProfile);
        log.info("translated_text_2 translated_text_2: {}", logPath);
        if ("local".equals(activeProfile)) {
            log.debug("translated_text_2 translated_text_1 execution translated_text_1 - translated_text_2 translated_text_3 information translated_text_2");
            log.debug("translated_text_2 translated_text_2 translated_text_2 appendertranslated_text_1 translated_text_2 translated_text_2");
        } else if ("test".equals(activeProfile)) {
            log.info("test translated_text_1 execution translated_text_1 - translated_text_2 translated_text_2 translated_text_2");
        }
        return Map.of("profile", activeProfile, "logPath", logPath, "fileLoggingEnabled", "local".equals(activeProfile));
    }
}