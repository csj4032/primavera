package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class MySQLContainerStrategy implements ContainerStrategy {
    
    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        if (!(config instanceof PrimaveraTestcontainersProperties.DatabaseConfig)) {
            throw new IllegalArgumentException("MySQL requires DatabaseConfig");
        }
        
        PrimaveraTestcontainersProperties.DatabaseConfig dbConfig = 
            (PrimaveraTestcontainersProperties.DatabaseConfig) config;
            
        String imageName = dbConfig.getDockerImageName() != null ? dbConfig.getDockerImageName() : "mysql:8.0";
        
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse(imageName))
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
        MySQLContainer<?> mysqlContainer = (MySQLContainer<?>) container;
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "spring.datasource.url=" + mysqlContainer.getJdbcUrl(),
                "spring.datasource.username=" + mysqlContainer.getUsername(),
                "spring.datasource.password=" + mysqlContainer.getPassword(),
                "spring.datasource.driver-class-name=" + mysqlContainer.getDriverClassName()
        );
    }
    
    @Override
    public String getSupportedType() {
        return "mysql";
    }
}