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
    public abstract Map<String, Object> getSpringProperties(T container);

    @Override
    public void startContainer(ConfigurableApplicationContext applicationContext) {
        if (container == null) {
            container = createContainer();
            log.info("Starting {} container with image: {}", containerType.name(), container.getDockerImageName());
            container.start();
            
            // Wait for container to be fully ready
            if (!container.isRunning()) {
                throw new RuntimeException(containerType.name() + " container failed to start properly");
            }
            
            log.info("{} container started successfully at {}:{}", 
                    containerType.name(), container.getHost(), container.getFirstMappedPort());
            
            Map<String, Object> properties = getSpringProperties(container);
            String propertySourceName = containerType.name() + "TestcontainersProperties";
            MapPropertySource propertySource = new MapPropertySource(propertySourceName, properties);
            applicationContext.getEnvironment().getPropertySources().addFirst(propertySource);
            
            log.info("Added {} properties to Spring Environment with source name '{}': {}", 
                    containerType.name(), propertySourceName, properties);
            
            // 프로퍼티가 실제로 설정되었는지 확인
            properties.forEach((key, value) -> {
                String actualValue = applicationContext.getEnvironment().getProperty(key);
                log.info("Property verification: {} = {} (expected: {})", key, actualValue, value);
            });
        }
    }

    @Override
    public GenericContainer<?> getContainer() {
        if (container == null) {
            container = createContainer();
        }
        return container;
    }

    @Override
    public boolean isRunning() {
        return container != null && container.isRunning();
    }
}
