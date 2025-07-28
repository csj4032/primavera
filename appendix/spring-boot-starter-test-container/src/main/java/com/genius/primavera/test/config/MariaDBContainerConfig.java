package com.genius.primavera.test.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MariaDB TestContainer 설정
 */
@Data
@ConfigurationProperties(prefix = "primavera.testcontainers.mariadb")
public class MariaDBContainerConfig {
    
    private String image = "mariadb:11.4.7";
    private String databaseName = "primavera";
    private String username = "primavera";
    private String password = "primavera";
    private String driverClassName = "org.mariadb.jdbc.Driver";
    private String initScript;
    private boolean reuse = true;
}