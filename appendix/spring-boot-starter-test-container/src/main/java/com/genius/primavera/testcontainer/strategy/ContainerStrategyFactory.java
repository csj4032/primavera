package com.genius.primavera.testcontainer.strategy;

import com.genius.primavera.testcontainer.ContainerType;

public class ContainerStrategyFactory {
    
    public static ContainerStrategy getStrategy(ContainerType containerType) {
        return switch (containerType) {
            case MARIADB -> new MariaDBContainerStrategy();
            case MYSQL -> new MySQLContainerStrategy();
            case POSTGRESQL -> new PostgreSQLContainerStrategy();
            case REDIS -> new RedisContainerStrategy();
            case KAFKA -> new KafkaContainerStrategy();
            case ELASTICSEARCH -> new ElasticsearchContainerStrategy();
        };
    }
}