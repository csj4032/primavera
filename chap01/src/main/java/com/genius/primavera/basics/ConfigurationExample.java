package com.genius.primavera.basics;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot 설정 관리 예제
 * @Value와 @ConfigurationProperties 사용법을 보여줍니다.
 */
public class ConfigurationExample {

    /**
     * @Value 어노테이션을 사용한 설정값 주입 예제
     */
    @Component
    @Slf4j
    public static class ValueAnnotationExample {
        
        @Value("${app.name}")
        private String appName;
        
        @Value("${app.version}")
        private String appVersion;
        
        @Value("${app.debug}")
        private boolean debug;
        
        @Value("${app.max-users}")
        private int maxUsers;
        
        @Value("${app.features-string:}")
        private String featuresString;
        
        public void printConfiguration() {
            log.info("=== @Value 어노테이션 설정 ===");
            log.info("앱 이름: {}", appName);
            log.info("버전: {}", appVersion);
            log.info("디버그 모드: {}", debug);
            log.info("최대 사용자: {}", maxUsers);
            log.info("기능 목록: {}", getFeatures());
        }
        
        public String getAppName() { return appName; }
        public String getAppVersion() { return appVersion; }
        public boolean isDebug() { return debug; }
        public int getMaxUsers() { return maxUsers; }
        public List<String> getFeatures() { 
            return featuresString.isEmpty() ? List.of() : Arrays.asList(featuresString.split(","));
        }
    }

    /**
     * @ConfigurationProperties를 사용한 타입 안전한 설정 예제
     */
    @Component
    @ConfigurationProperties(prefix = "app")
    @Data
    public static class AppProperties {
        
        private String name;
        private String version;
        private boolean debug;
        private int maxUsers;
        private List<String> features;
        private Database database;
        private Map<String, String> metadata;
        
        @Data
        public static class Database {
            private String url;
            private String username;
            private String password;
            private int maxConnections;
        }
    }
}