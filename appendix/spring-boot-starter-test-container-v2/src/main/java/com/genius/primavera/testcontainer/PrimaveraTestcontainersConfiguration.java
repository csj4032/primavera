package com.genius.primavera.testcontainer;

import com.genius.primavera.testcontainer.strategy.ContainerStrategy;
import com.genius.primavera.testcontainer.strategy.ContainerStrategyFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;

@Configuration
@EnableConfigurationProperties(PrimaveraTestcontainersProperties.class)
public class PrimaveraTestcontainersConfiguration {

    @Autowired
    private PrimaveraTestcontainersProperties properties;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void initializeContainers() {
        String testClassName = getCurrentTestClassName();
        properties.getContainers().forEach((containerType, config) -> {
            if (config.isEnabled()) {
                initializeContainer((ConfigurableApplicationContext) applicationContext, containerType, config, properties.getLifecycleMode(), testClassName);
            }
        });
    }

    private void initializeContainer(ConfigurableApplicationContext applicationContext, String containerType, PrimaveraTestcontainersProperties.ContainerConfig config, ContainerLifecycleMode lifecycleMode, String testClassName) {
        ContainerStrategy strategy = ContainerStrategyFactory.getStrategy(containerType);
        if (strategy == null) throw new IllegalArgumentException("Unsupported container type: " + containerType);
        if (!ContainerManager.containsContainer(containerType, lifecycleMode, testClassName)) {
            try {
                GenericContainer<?> container = strategy.createContainer(config);
                container.start();
                ContainerManager.putContainer(containerType, container, lifecycleMode, testClassName);
                strategy.configureApplicationContext(applicationContext, container);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize container: " + containerType, e);
            }
        } else {
            GenericContainer<?> existingContainer = ContainerManager.getContainer(containerType, lifecycleMode, testClassName);
            strategy.configureApplicationContext(applicationContext, existingContainer);
        }
    }

    private String getCurrentTestClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("Test") && !className.contains("Spring") && !className.contains("junit") && !className.contains("PrimaveraTestcontainers")) return className;
        }
        return "UnknownTestClass";
    }
}