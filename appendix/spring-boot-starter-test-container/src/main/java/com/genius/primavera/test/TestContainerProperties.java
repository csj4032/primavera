package com.genius.primavera.test;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "primavera.testcontainers")
public class TestContainerProperties {
    
    private boolean enabled = true;
    
    private MariaDB mariadb = new MariaDB();
    
    @Data
    public static class MariaDB {
        private String imageName = "mariadb:11.4.7";
        private String databaseName = "primavera";
        private String username = "primavera";
        private String password = "primavera";
        private boolean reuse = true;
        private Map<String, String> urlParams = new HashMap<>() {{
            put("allowPublicKeyRetrieval", "true");
            put("useSSL", "false");
            put("serverTimezone", "UTC");
            put("characterEncoding", "UTF-8");
        }};
        private String initScript = "sql/schema.sql";
    }
}