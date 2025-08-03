package com.genius.primavera.testcontainer.v2.configurator;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.JdbcDatabaseContainer;

@Slf4j
public abstract class AbstractDatabasePropertyConfigurator implements PropertyConfigurator {
    
    protected final void setDatabaseProperties(JdbcDatabaseContainer<?> container, String driverClassName) {
        String jdbcUrl = container.getJdbcUrl();
        String username = container.getUsername();
        String password = container.getPassword();
        
        System.setProperty("spring.datasource.url", jdbcUrl);
        System.setProperty("spring.datasource.username", username);
        System.setProperty("spring.datasource.password", password);
        System.setProperty("spring.datasource.driver-class-name", driverClassName);
        
        log.info("Set {} properties - URL: {}, Username: {}", getContainerType(), jdbcUrl, username);
    }
    
    protected abstract String getContainerType();
}