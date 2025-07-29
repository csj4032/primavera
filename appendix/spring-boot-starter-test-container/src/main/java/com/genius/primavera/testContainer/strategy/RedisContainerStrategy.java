package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.config.RedisContainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

@Slf4j
public class RedisContainerStrategy extends AbstractContainerStrategy<GenericContainer<?>> {

    public RedisContainerStrategy(RedisContainerConfig config) {
        super(ContainerType.REDIS, config);
    }
}
