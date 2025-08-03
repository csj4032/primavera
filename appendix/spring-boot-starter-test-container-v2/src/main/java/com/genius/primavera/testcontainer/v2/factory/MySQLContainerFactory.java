package com.genius.primavera.testcontainer.v2.factory;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class MySQLContainerFactory implements ContainerBuilderFactory {
    
    @Override
    public GenericContainer<?> createContainer(TestContainerProperties.ContainerConfig config) {
        TestContainerProperties.MySQLConfig mysqlConfig = (TestContainerProperties.MySQLConfig) config;
        
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse(mysqlConfig.getDockerImageName()))
                .withDatabaseName(mysqlConfig.getDatabaseName())
                .withUsername(mysqlConfig.getUsername())
                .withPassword(mysqlConfig.getPassword());
        
        if (mysqlConfig.getInitScript() != null && !mysqlConfig.getInitScript().isEmpty()) {
            container.withInitScript(mysqlConfig.getInitScript());
        }
        
        log.info("Created MySQL container with image: {}, database: {}", 
                 mysqlConfig.getDockerImageName(), mysqlConfig.getDatabaseName());
        return container;
    }
    
    @Override
    public boolean supports(ContainerType containerType) {
        return containerType == ContainerType.MYSQL;
    }
}