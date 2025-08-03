package com.genius.primavera.testcontainer.v2.factory;

import com.genius.primavera.testcontainer.v2.*;
import com.genius.primavera.testcontainer.v2.builder.MariaDBContainerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

@Slf4j
public class MariaDBContainerFactory implements ContainerBuilderFactory {
    
    @Override
    public GenericContainer<?> createContainer(TestContainerProperties.ContainerConfig config) {
        TestContainerProperties.MariaDBConfig mariadbConfig = (TestContainerProperties.MariaDBConfig) config;
        
        GenericContainer<?> container = new MariaDBContainerBuilder()
                .withImage(mariadbConfig.getDockerImageName())
                .withDatabase(mariadbConfig.getDatabaseName())
                .withCredentials(mariadbConfig.getUsername(), mariadbConfig.getPassword())
                .withInitScript(mariadbConfig.getInitScript())
                .build();
        
        log.info("Created MariaDB container with image: {}, database: {}", 
                 mariadbConfig.getDockerImageName(), mariadbConfig.getDatabaseName());
        return container;
    }
    
    @Override
    public boolean supports(ContainerType containerType) {
        return containerType == ContainerType.MARIADB;
    }
}