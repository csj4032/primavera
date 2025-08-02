package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MariaDBContainerStrategy implements ContainerStrategy {
    
    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        String imageName = config.getDockerImageName() != null ? config.getDockerImageName() : "mariadb:11.4.7";
        
        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(imageName))
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
        MariaDBContainer<?> mariadbContainer = (MariaDBContainer<?>) container;
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "spring.datasource.url=" + mariadbContainer.getJdbcUrl(),
                "spring.datasource.username=" + mariadbContainer.getUsername(),
                "spring.datasource.password=" + mariadbContainer.getPassword(),
                "spring.datasource.driver-class-name=" + mariadbContainer.getDriverClassName()
        );
    }
    
    @Override
    public String getSupportedType() {
        return "mariadb";
    }
}