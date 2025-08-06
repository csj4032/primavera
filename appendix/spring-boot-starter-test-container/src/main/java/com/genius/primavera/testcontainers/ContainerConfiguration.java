package com.genius.primavera.testcontainers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "testcontainers")
public class ContainerConfiguration {

    private Map<String, ContainerSpec> containers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContainerSpec {
        private String image;
        private String database;
        private String username;
        private String password;
        private String initScript;
        private Integer startupTimeout;
        private Map<String, String> environment;
        private String[] networkAliases;

        public String getImageOrDefault(ContainerType type) {
            return image != null ? image : type.getDefaultImage();
        }

        public String getUsernameOrDefault() {
            return username != null ? username : "test";
        }

        public String getPasswordOrDefault() {
            return password != null ? password : "test";
        }

        public String getDatabaseOrDefault() {
            return database != null ? database : "test";
        }

        public Integer getStartupTimeoutOrDefault() {
            return startupTimeout != null ? startupTimeout : 60;
        }
    }
}