package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;

public class ContainerStrategyFactory {
    
    public static ContainerStrategy getStrategy(ContainerType containerType) {
        return switch (containerType) {
            case MARIADB -> new MariaDBContainerStrategy();
            case MYSQL -> new MySQLContainerStrategy();
            case POSTGRESQL -> new PostgreSQLContainerStrategy();
            case REDIS -> new RedisContainerStrategy();
            case KAFKA -> new KafkaContainerStrategy();
            case ELASTICSEARCH -> new ElasticsearchContainerStrategy();
            case MONGODB -> new MongoDBStrategy();
        };
    }
}