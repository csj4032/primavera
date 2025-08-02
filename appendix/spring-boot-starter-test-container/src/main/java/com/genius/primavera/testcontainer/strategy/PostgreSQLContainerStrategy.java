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
        if (!(config instanceof PrimaveraTestcontainersProperties.DatabaseConfig)) {
            throw new IllegalArgumentException("PostgreSQL requires DatabaseConfig");
        }
        
        PrimaveraTestcontainersProperties.DatabaseConfig dbConfig = 
            (PrimaveraTestcontainersProperties.DatabaseConfig) config;
            
        String imageName = dbConfig.getDockerImageName() != null ? dbConfig.getDockerImageName() : "postgres:15";
        
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(imageName))
                .withDatabaseName(dbConfig.getDatabaseName() != null ? dbConfig.getDatabaseName() : "primavera")
                .withUsername(dbConfig.getUsername() != null ? dbConfig.getUsername() : "primavera")
                .withPassword(dbConfig.getPassword() != null ? dbConfig.getPassword() : "primavera");
        
        if (dbConfig.getInitScript() != null && !dbConfig.getInitScript().isEmpty()) {
            container.withInitScript(dbConfig.getInitScript());
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