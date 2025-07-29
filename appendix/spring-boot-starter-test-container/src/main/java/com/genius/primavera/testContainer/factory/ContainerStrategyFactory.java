package com.genius.primavera.testContainer.factory;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import com.genius.primavera.testContainer.config.KafkaContainerConfig;
import com.genius.primavera.testContainer.config.MariaDBContainerConfig;
import com.genius.primavera.testContainer.config.PostgreSQLContainerConfig;
import com.genius.primavera.testContainer.config.RedisContainerConfig;
import com.genius.primavera.testContainer.strategy.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class ContainerStrategyFactory {

    private final Map<ContainerType, Supplier<ContainerStrategy>> strategySuppliers = new HashMap<>();

    public ContainerStrategyFactory(Environment environment) {
        strategySuppliers.put(ContainerType.MARIADB, () -> new MariaDBContainerStrategy(new MariaDBContainerConfig()));
        strategySuppliers.put(ContainerType.REDIS, () -> new RedisContainerStrategy(new RedisContainerConfig()));
        strategySuppliers.put(ContainerType.KAFKA, () -> new KafkaContainerStrategy(new KafkaContainerConfig()));
        strategySuppliers.put(ContainerType.POSTGRESQL, () -> new PostgreSQLContainerStrategy(new PostgreSQLContainerConfig()));
    }

    public ContainerStrategy getStrategy(ContainerType type) {
        Supplier<ContainerStrategy> supplier = strategySuppliers.get(type);
        if (supplier == null) {
            throw new IllegalArgumentException("No strategy found for container type: " + type);
        }
        return supplier.get();
    }
}