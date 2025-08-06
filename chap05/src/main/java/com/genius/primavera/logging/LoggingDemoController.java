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
        log.trace("TRACE 레벨 - 가장 상세한 정보");
        log.debug("DEBUG 레벨 - 디버깅 정보");
        log.info("INFO 레벨 - 일반 정보 메시지");
        log.warn("WARN 레벨 - 경고 메시지");
        log.error("ERROR 레벨 - 에러 메시지");
        String user = "사용자";
        LocalDateTime now = LocalDateTime.now();
        log.info("사용자 [{}]가 {}에 로깅 데모를 실행했습니다", user, now);
        try {
            throw new RuntimeException("의도적인 예외 발생");
        } catch (Exception e) {
            log.error("예외가 발생했습니다", e);
        }
        return Map.of("activeProfile", activeProfile, "logPath", logPath, "timestamp", now, "message", "로그 파일을 확인하세요: " + logPath);
    }

    @GetMapping("/profile-info")
    public Map<String, Object> getProfileInfo() {
        log.info("현재 활성 프로파일: {}", activeProfile);
        log.info("로그 경로: {}", logPath);
        if ("local".equals(activeProfile)) {
            log.debug("로컬 환경에서 실행 중 - 상세 디버그 정보 출력");
            log.debug("파일 기반 로그 appender가 모두 활성화됨");
        } else if ("test".equals(activeProfile)) {
            log.info("테스트 환경에서 실행 중 - 콘솔 로그만 활성화");
        }
        return Map.of("profile", activeProfile, "logPath", logPath, "fileLoggingEnabled", "local".equals(activeProfile));
    }
}