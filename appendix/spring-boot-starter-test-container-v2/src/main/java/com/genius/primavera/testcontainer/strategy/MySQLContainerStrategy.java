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
        String imageName = config.getDockerImageName() != null ? config.getDockerImageName() : "mysql:8.0";
        
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse(imageName))
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