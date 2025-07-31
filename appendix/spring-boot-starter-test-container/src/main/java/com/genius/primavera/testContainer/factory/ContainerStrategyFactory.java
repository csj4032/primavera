package com.genius.primavera.testContainer.factory;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import com.genius.primavera.testContainer.strategy.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

@Slf4j
public class ContainerStrategyFactory {

    private final Environment environment;
    private PrimaveraTestcontainersProperties properties;

    public ContainerStrategyFactory(Environment environment) {
        this.environment = environment;
    }

    private PrimaveraTestcontainersProperties getProperties() {
        if (this.properties == null) {
            this.properties = Binder.get(environment)
                .bind("primavera.testcontainers", PrimaveraTestcontainersProperties.class)
                .orElseGet(PrimaveraTestcontainersProperties::new);
            log.debug("Loaded PrimaveraTestcontainersProperties: {}", properties);
        }
        return this.properties;
    }

    public ContainerStrategy getStrategy(ContainerType type) {
        PrimaveraTestcontainersProperties props = getProperties();
        
        return switch (type) {
            case MARIADB -> new MariaDBContainerStrategy(environment, props.getMariadb());
            case REDIS -> new RedisContainerStrategy(environment, props.getRedis());
            case KAFKA -> new KafkaContainerStrategy(environment, props.getKafka());
            case POSTGRESQL -> new PostgreSQLContainerStrategy(environment, props.getPostgreSQL());
        };
    }
}