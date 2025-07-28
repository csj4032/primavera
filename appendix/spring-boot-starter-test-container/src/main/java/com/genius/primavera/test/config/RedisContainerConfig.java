package com.genius.primavera.test.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis TestContainer 설정
 */
@Data
@ConfigurationProperties(prefix = "primavera.testcontainers.redis")
public class RedisContainerConfig {
    
    private String image = "redis:7-alpine";
    private int port = 6379;
    private String password;
    private boolean reuse = true;
}