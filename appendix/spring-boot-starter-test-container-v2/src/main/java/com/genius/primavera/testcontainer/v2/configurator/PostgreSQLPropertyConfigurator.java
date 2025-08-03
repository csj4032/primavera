package com.genius.primavera.testcontainer.v2.configurator;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLPropertyConfigurator extends AbstractDatabasePropertyConfigurator {
    
    @Override
    public void configureSpringProperties(GenericContainer<?> container) {
        PostgreSQLContainer<?> postgresqlContainer = (PostgreSQLContainer<?>) container;
        setDatabaseProperties(postgresqlContainer, "org.postgresql.Driver");
    }
    
    @Override
    public boolean supports(Class<? extends GenericContainer<?>> containerClass) {
        return PostgreSQLContainer.class.isAssignableFrom(containerClass);
    }
    
    @Override
    protected String getContainerType() {
        return "PostgreSQL";
    }
}