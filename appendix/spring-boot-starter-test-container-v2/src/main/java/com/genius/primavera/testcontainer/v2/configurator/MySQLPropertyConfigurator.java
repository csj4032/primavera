package com.genius.primavera.testcontainer.v2.configurator;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

public class MySQLPropertyConfigurator extends AbstractDatabasePropertyConfigurator {
    
    @Override
    public void configureSpringProperties(GenericContainer<?> container) {
        MySQLContainer<?> mysqlContainer = (MySQLContainer<?>) container;
        setDatabaseProperties(mysqlContainer, "com.mysql.cj.jdbc.Driver");
    }
    
    @Override
    public boolean supports(Class<? extends GenericContainer<?>> containerClass) {
        return MySQLContainer.class.isAssignableFrom(containerClass);
    }
    
    @Override
    protected String getContainerType() {
        return "MySQL";
    }
}