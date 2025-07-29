package com.genius.primavera.testContainer;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Primavera TestContainers 설정 프로퍼티
 * 
 * application.yml에서 다음과 같이 설정할 수 있습니다:
 * 
 * primavera:
 *   testcontainers:
 *     startup-timeout: 90s
 *     auto-stop: true
 *     reuse: true
 *     containers:
 *       mariadb:
 *         image: "mariadb:11.4.7"
 *         database: "custom_db"
 *         username: "custom_user"
 *         password: "custom_pass"
 *       redis:
 *         image: "redis:7-alpine"
 */
@ConfigurationProperties(prefix = "primavera.testcontainers")
public class PrimaveraTestcontainersProperties {
    
    /**
     * 컨테이너 시작 타임아웃
     */
    private Duration startupTimeout = Duration.ofSeconds(60);
    
    /**
     * 테스트 종료 후 컨테이너 자동 정지 여부
     */
    private boolean autoStop = true;
    
    /**
     * 컨테이너 재사용 여부
     */
    private boolean reuse = true;
    
    /**
     * 컨테이너별 개별 설정
     */
    private Map<String, ContainerConfig> containers = new HashMap<>();
    
    public PrimaveraTestcontainersProperties() {
        // 기본 컨테이너 설정 초기화
        initializeDefaultContainers();
    }
    
    private void initializeDefaultContainers() {
        // MariaDB 기본 설정
        ContainerConfig mariadbConfig = new ContainerConfig();
        mariadbConfig.setImage("mariadb:11.4.7");
        mariadbConfig.setDatabase("primavera");
        mariadbConfig.setUsername("primavera");
        mariadbConfig.setPassword("primavera");
        containers.put("mariadb", mariadbConfig);
        
        // Redis 기본 설정
        ContainerConfig redisConfig = new ContainerConfig();
        redisConfig.setImage("redis:7-alpine");
        containers.put("redis", redisConfig);
        
        // Kafka 기본 설정
        ContainerConfig kafkaConfig = new ContainerConfig();
        kafkaConfig.setImage("confluentinc/cp-kafka:latest");
        containers.put("kafka", kafkaConfig);
        
        // PostgreSQL 기본 설정
        ContainerConfig postgresqlConfig = new ContainerConfig();
        postgresqlConfig.setImage("postgres:15-alpine");
        postgresqlConfig.setDatabase("testdb");
        postgresqlConfig.setUsername("test");
        postgresqlConfig.setPassword("test");
        containers.put("postgresql", postgresqlConfig);
    }
    
    // Getters and Setters
    public Duration getStartupTimeout() {
        return startupTimeout;
    }
    
    public void setStartupTimeout(Duration startupTimeout) {
        this.startupTimeout = startupTimeout;
    }
    
    public boolean isAutoStop() {
        return autoStop;
    }
    
    public void setAutoStop(boolean autoStop) {
        this.autoStop = autoStop;
    }
    
    public boolean isReuse() {
        return reuse;
    }
    
    public void setReuse(boolean reuse) {
        this.reuse = reuse;
    }
    
    public Map<String, ContainerConfig> getContainers() {
        return containers;
    }
    
    public void setContainers(Map<String, ContainerConfig> containers) {
        this.containers = containers;
    }
    
    /**
     * 개별 컨테이너 설정
     */
    public static class ContainerConfig {
        private String image;
        private String database;
        private String username;
        private String password;
        private Map<String, String> environment = new HashMap<>();
        private Map<String, Object> properties = new HashMap<>();
        
        // Getters and Setters
        public String getImage() {
            return image;
        }
        
        public void setImage(String image) {
            this.image = image;
        }
        
        public String getDatabase() {
            return database;
        }
        
        public void setDatabase(String database) {
            this.database = database;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public Map<String, String> getEnvironment() {
            return environment;
        }
        
        public void setEnvironment(Map<String, String> environment) {
            this.environment = environment;
        }
        
        public Map<String, Object> getProperties() {
            return properties;
        }
        
        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }
    }
}