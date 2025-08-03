package com.genius.primavera.testcontainer.v2.configurator;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

public class MariaDBPropertyConfigurator extends AbstractDatabasePropertyConfigurator {
    
    @Override
    public void configureSpringProperties(GenericContainer<?> container) {
        MariaDBContainer<?> mariadbContainer = (MariaDBContainer<?>) container;
        setDatabaseProperties(mariadbContainer, "org.mariadb.jdbc.Driver");
    }
    
    @Override
    public boolean supports(Class<? extends GenericContainer<?>> containerClass) {
        return MariaDBContainer.class.isAssignableFrom(containerClass);
    }
    
    @Override
    protected String getContainerType() {
        return "MariaDB";
    }
}