package com.genius.primavera.testcontainer;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

public class PrimaveraTestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static MariaDBContainer<?> mariaDBContainer;
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        PrimaveraTestcontainersProperties properties = Binder.get(environment)
                .bind("primavera.testcontainers.mariadb", PrimaveraTestcontainersProperties.class)
                .orElse(new PrimaveraTestcontainersProperties());
        
        if (!properties.isEnabled()) {
            return;
        }
        
        if (mariaDBContainer == null) {
            mariaDBContainer = createMariaDBContainer(properties);
            mariaDBContainer.start();
        }
        
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "spring.datasource.url=" + mariaDBContainer.getJdbcUrl(),
                "spring.datasource.username=" + mariaDBContainer.getUsername(),
                "spring.datasource.password=" + mariaDBContainer.getPassword(),
                "spring.datasource.driver-class-name=" + mariaDBContainer.getDriverClassName()
        );
    }
    
    private MariaDBContainer<?> createMariaDBContainer(PrimaveraTestcontainersProperties properties) {
        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(properties.getDockerImageName()))
                .withDatabaseName(properties.getDatabaseName())
                .withUsername(properties.getUsername())
                .withPassword(properties.getPassword());
        
        if (properties.getInitScript() != null && !properties.getInitScript().isEmpty()) {
            container.withInitScript(properties.getInitScript());
        }
        
        return container;
    }
    
    public static void stopContainer() {
        if (mariaDBContainer != null && mariaDBContainer.isRunning()) {
            mariaDBContainer.stop();
            mariaDBContainer = null;
        }
    }
}