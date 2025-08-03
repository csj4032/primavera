package com.genius.primavera.testcontainer.v2.factory;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class PostgreSQLContainerFactory implements ContainerBuilderFactory {
    
    @Override
    public GenericContainer<?> createContainer(TestContainerProperties.ContainerConfig config) {
        TestContainerProperties.PostgreSQLConfig postgresqlConfig = (TestContainerProperties.PostgreSQLConfig) config;
        
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(postgresqlConfig.getDockerImageName()))
                .withDatabaseName(postgresqlConfig.getDatabaseName())
                .withUsername(postgresqlConfig.getUsername())
                .withPassword(postgresqlConfig.getPassword());
        
        if (postgresqlConfig.getInitScript() != null && !postgresqlConfig.getInitScript().isEmpty()) {
            container.withInitScript(postgresqlConfig.getInitScript());
        }
        
        log.info("Created PostgreSQL container with image: {}, database: {}", 
                 postgresqlConfig.getDockerImageName(), postgresqlConfig.getDatabaseName());
        return container;
    }
    
    @Override
    public boolean supports(ContainerType containerType) {
        return containerType == ContainerType.POSTGRESQL;
    }
}