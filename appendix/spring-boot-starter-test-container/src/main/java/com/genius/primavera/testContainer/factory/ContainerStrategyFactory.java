package com.genius.primavera.testContainer.factory;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import com.genius.primavera.testContainer.config.*;
import com.genius.primavera.testContainer.strategy.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Component
public class ContainerStrategyFactory {

    private final Map<ContainerType, Supplier<ContainerStrategy>> strategySuppliers = new HashMap<>();

    public ContainerStrategyFactory(PrimaveraTestcontainersProperties properties) {
        strategySuppliers.put(ContainerType.MARIADB, () -> new MariaDBContainerStrategy(new MariaDBContainerConfig(properties)));
        strategySuppliers.put(ContainerType.REDIS, () -> new RedisContainerStrategy(new RedisContainerConfig(properties)));
        strategySuppliers.put(ContainerType.KAFKA, () -> new KafkaContainerStrategy(new KafkaContainerConfig(properties)));
        strategySuppliers.put(ContainerType.POSTGRESQL, () -> new PostgreSQLContainerStrategy(new PostgreSQLContainerConfig(properties)));
    }

    public ContainerStrategy getStrategy(ContainerType type) {
        Supplier<ContainerStrategy> supplier = strategySuppliers.get(type);
        if (supplier == null) {
            throw new IllegalArgumentException("No strategy found for container type: " + type);
        }
        return supplier.get();
    }
}