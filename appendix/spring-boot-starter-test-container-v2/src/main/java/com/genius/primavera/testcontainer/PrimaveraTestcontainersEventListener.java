package com.genius.primavera.testcontainer;

import com.genius.primavera.testcontainer.strategy.ContainerStrategy;
import com.genius.primavera.testcontainer.strategy.ContainerStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;

@Component
public class PrimaveraTestcontainersEventListener implements ApplicationListener<ApplicationContextInitializedEvent> {

    @Autowired(required = false)
    private PrimaveraTestcontainersProperties properties;

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        if (properties == null) return;
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        String testClassName = getCurrentTestClassName();
        properties.getContainers().forEach((containerType, config) -> {
            System.out.println("🔍 컨테이너 처리 중: " + containerType + ", 활성화: " + config.isEnabled());
            if (config.isEnabled()) {
                initializeContainer(applicationContext, containerType, config, properties.getLifecycleMode(), testClassName);
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