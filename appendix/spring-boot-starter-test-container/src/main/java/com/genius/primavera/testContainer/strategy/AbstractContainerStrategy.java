package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

@Slf4j
public abstract class AbstractContainerStrategy<T extends GenericContainer<?>> implements ContainerStrategy {

    protected final ContainerType containerType;
    protected final Environment environment;
    protected T container;

    public AbstractContainerStrategy(ContainerType containerType, Environment environment) {
        this.containerType = containerType;
        this.environment = environment;
    }

    @Override
    public ContainerType getContainerType() {
        return containerType;
    }

    protected abstract T createContainer();
    protected abstract Map<String, Object> getSpringProperties(T container);

    @Override
    public void startContainer(ConfigurableApplicationContext applicationContext) {
        if (container == null) {
            container = createContainer();
            container.start();
            log.info("{} container started at {}:{}", containerType.name(), container.getHost(), container.getFirstMappedPort());
            Map<String, Object> properties = getSpringProperties(container);
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
