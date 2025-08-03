package com.genius.primavera.testcontainer.v2.factory;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class MongoDBContainerFactory implements ContainerBuilderFactory {
    
    @Override
    public GenericContainer<?> createContainer(TestContainerProperties.ContainerConfig config) {
        TestContainerProperties.MongoDBConfig mongodbConfig = (TestContainerProperties.MongoDBConfig) config;
        
        MongoDBContainer container = new MongoDBContainer(DockerImageName.parse(mongodbConfig.getDockerImageName()));
        
        if (mongodbConfig.getUsername() != null && mongodbConfig.getPassword() != null) {
            container.withEnv("MONGO_INITDB_ROOT_USERNAME", mongodbConfig.getUsername())
                    .withEnv("MONGO_INITDB_ROOT_PASSWORD", mongodbConfig.getPassword());
        }
        
        if (mongodbConfig.getDatabaseName() != null) {
            container.withEnv("MONGO_INITDB_DATABASE", mongodbConfig.getDatabaseName());
        }
        
        log.info("Created MongoDB container with image: {}, database: {}", 
                 mongodbConfig.getDockerImageName(), mongodbConfig.getDatabaseName());
        return container;
    }
    
    @Override
    public boolean supports(ContainerType containerType) {
        return containerType == ContainerType.MONGODB;
    }
}