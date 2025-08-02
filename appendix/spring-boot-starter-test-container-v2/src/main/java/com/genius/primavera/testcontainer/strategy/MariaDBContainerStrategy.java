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
        // ContainerConfig를 DatabaseConfig로 캐스팅
        if (!(config instanceof PrimaveraTestcontainersProperties.DatabaseConfig)) {
            throw new IllegalArgumentException("MariaDB requires DatabaseConfig");
        }
        
        PrimaveraTestcontainersProperties.DatabaseConfig dbConfig = 
            (PrimaveraTestcontainersProperties.DatabaseConfig) config;
            
        String imageName = dbConfig.getDockerImageName() != null ? dbConfig.getDockerImageName() : "mariadb:11.4.7";
        
        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(imageName))
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