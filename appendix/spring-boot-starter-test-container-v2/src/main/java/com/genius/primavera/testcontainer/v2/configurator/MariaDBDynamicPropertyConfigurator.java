package com.genius.primavera.testcontainer.v2.configurator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

@Slf4j
public class MariaDBDynamicPropertyConfigurator implements DynamicPropertyConfigurator {
    
    @Override
    public void configureDynamicProperties(GenericContainer<?> container, DynamicPropertyRegistry registry) {
        MariaDBContainer<?> mariadbContainer = (MariaDBContainer<?>) container;
        
        registry.add("spring.datasource.url", mariadbContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mariadbContainer::getUsername);
        registry.add("spring.datasource.password", mariadbContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        
        log.info("Configured MariaDB dynamic properties");
    }
    
    @Override
    public boolean supports(Class<? extends GenericContainer<?>> containerClass) {
        return MariaDBContainer.class.isAssignableFrom(containerClass);
    }
}