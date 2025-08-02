package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgreSQLContainerStrategy implements ContainerStrategy {
    
    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        String imageName = config.getDockerImageName() != null ? config.getDockerImageName() : "postgres:15";
        
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(imageName))
                .withDatabaseName(config.getDatabaseName() != null ? config.getDatabaseName() : "primavera")
                .withUsername(config.getUsername() != null ? config.getUsername() : "primavera")
                .withPassword(config.getPassword() != null ? config.getPassword() : "primavera");
        
        if (config.getInitScript() != null && !config.getInitScript().isEmpty()) {
            container.withInitScript(config.getInitScript());
        }
        
        return container;
    }
    
    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        PostgreSQLContainer<?> postgresContainer = (PostgreSQLContainer<?>) container;
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                "spring.datasource.username=" + postgresContainer.getUsername(),
                "spring.datasource.password=" + postgresContainer.getPassword(),
                "spring.datasource.driver-class-name=" + postgresContainer.getDriverClassName()
        );
    }
    
    @Override
    public String getSupportedType() {
        return "postgresql";
    }
}