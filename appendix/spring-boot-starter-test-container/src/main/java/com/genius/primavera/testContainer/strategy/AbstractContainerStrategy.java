package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.config.ContainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

@Slf4j
public class AbstractContainerStrategy<T extends GenericContainer<?>> implements ContainerStrategy {

    protected final ContainerType containerType;
    protected final ContainerConfig<T> config;
    protected T container;

    public AbstractContainerStrategy(ContainerType containerType, ContainerConfig<T> config) {
        this.containerType = containerType;
        this.config = config;
    }

    @Override
    public ContainerType getContainerType() {
        return containerType;
    }

    @Override
    public void startContainer(ConfigurableApplicationContext applicationContext) {
        if (container == null) {
            container = config.createContainer(applicationContext.getEnvironment());
            container.start();
            log.info("{} container started at {}:{}", containerType.name(), container.getHost(), container.getFirstMappedPort());
            Map<String, Object> properties = config.getSpringProperties(container, applicationContext.getEnvironment());
            applicationContext.getEnvironment().getPropertySources().addFirst(new MapPropertySource(containerType.name() + "TestcontainersProperties", properties));
            log.info("Added {} properties to Spring Environment: {}", containerType.name(), properties.keySet());
        }
    }

    @Override
    public GenericContainer<?> getContainer() {
        return container;
    }

    @Override
    public boolean isRunning() {
        return container != null && container.isRunning();
    }
}
