package com.genius.primavera.testcontainer.v2.factory;

import com.genius.primavera.testcontainer.v2.*;
import org.testcontainers.containers.GenericContainer;

public interface ContainerBuilderFactory {
    
    GenericContainer<?> createContainer(TestContainerProperties.ContainerConfig config);
    
    boolean supports(ContainerType containerType);
    
    static ContainerBuilderFactory getFactory(ContainerType containerType) {
        return switch (containerType) {
            case MARIADB -> new MariaDBContainerFactory();
            case MYSQL -> new MySQLContainerFactory();
            case POSTGRESQL -> new PostgreSQLContainerFactory();
            case REDIS -> new RedisContainerFactory();
            case KAFKA -> new KafkaContainerFactory();
            case ELASTICSEARCH -> new ElasticsearchContainerFactory();
            case MONGODB -> new MongoDBContainerFactory();
        };
    }
}