package com.genius.primavera.basics;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigurationExample {

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
            log.info("=== @Value annotation test ===");
            log.info("should test: {}", appName);
            log.info("test: {}", appVersion);
            log.info("connection test: {}", debug);
            log.info("test user: {}", maxUsers);
            log.info("test: {}", getFeatures());
        }
        
        public String getAppName() { return appName; }
        public String getAppVersion() { return appVersion; }
        public boolean isDebug() { return debug; }
        public int getMaxUsers() { return maxUsers; }
        public List<String> getFeatures() { 
            return featuresString.isEmpty() ? List.of() : Arrays.asList(featuresString.split(","));
        }
    }

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