package com.genius.primavera.testContainer.factory;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.config.*;
import com.genius.primavera.testContainer.strategy.*;

import java.util.Map;

/**
 * ContainerStrategy 생성을 위한 Factory 클래스
 */
public class ContainerStrategyFactory {
    
    private final Map<ContainerType, ContainerStrategy> strategyCache;
    
    public ContainerStrategyFactory(
            MariaDBContainerConfig mariaDBConfig,
            RedisContainerConfig redisConfig,
            KafkaContainerConfig kafkaConfig,
            PostgreSQLContainerConfig postgreSQLConfig) {
        this.strategyCache = Map.of(
            ContainerType.MARIADB, new MariaDBContainerStrategy(mariaDBConfig),
            ContainerType.REDIS, new RedisContainerStrategy(redisConfig),
            ContainerType.KAFKA, new KafkaContainerStrategy(kafkaConfig),
            ContainerType.POSTGRESQL, new PostgreSQLContainerStrategy(postgreSQLConfig)
        );
    }
    
    public ContainerStrategy getStrategy(ContainerType containerType) {
        if (containerType == null) {
            throw new IllegalArgumentException("Container type cannot be null");
        }
        
        ContainerStrategy strategy = strategyCache.get(containerType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported container type: " + containerType);
        }
        return strategy;
    }
}