package com.genius.primavera.testcontainers.strategy;

import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.strategy.impl.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for container type strategies
 * Centralizes strategy management and eliminates switch-case duplication
 */
@Slf4j
public class ContainerTypeStrategyRegistry {
    
    private static final Map<ContainerType, ContainerTypeStrategy> STRATEGIES = new ConcurrentHashMap<>();
    
    static {
        registerStrategy(new MariaDBStrategy());
        registerStrategy(new MySQLStrategy());
        registerStrategy(new PostgreSQLStrategy());
        registerStrategy(new RedisStrategy());
        registerStrategy(new MongoDBStrategy());
        registerStrategy(new KafkaStrategy());
        registerStrategy(new ElasticsearchStrategy());
        registerStrategy(new VaultStrategy());
        registerStrategy(new LocalStackStrategy());
    }
    
    private static void registerStrategy(ContainerTypeStrategy strategy) {
        STRATEGIES.put(strategy.getSupportedType(), strategy);
        log.debug("Registered strategy for container type: {}", strategy.getSupportedType());
    }
    
    /**
     * Retrieves strategy for the given container type
     */
    public static Optional<ContainerTypeStrategy> getStrategy(ContainerType type) {
        return Optional.ofNullable(STRATEGIES.get(type));
    }
    
    /**
     * Checks if a strategy exists for the given container type
     */
    public static boolean hasStrategy(ContainerType type) {
        return STRATEGIES.containsKey(type);
    }
    
    /**
     * Gets strategy or throws exception if not found
     */
    public static ContainerTypeStrategy getRequiredStrategy(ContainerType type) {
        return getStrategy(type)
                .orElseThrow(() -> new UnsupportedOperationException("No strategy found for container type: " + type));
    }
}