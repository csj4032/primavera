package com.genius.primavera.test.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PostgreSQL TestContainer 설정
 */
@Data
@ConfigurationProperties(prefix = "primavera.testcontainers.postgresql")
public class PostgreSQLContainerConfig {
    
    private String image = "postgres:15-alpine";
    private String databaseName = "testdb";
    private String username = "test";
    private String password = "test";
    private String driverClassName = "org.postgresql.Driver";
    private String initScript;
    private boolean reuse = true;
}