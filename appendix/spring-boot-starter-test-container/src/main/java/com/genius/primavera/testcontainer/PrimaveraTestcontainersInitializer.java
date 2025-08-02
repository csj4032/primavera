package com.genius.primavera.testcontainer;

import com.genius.primavera.testcontainer.strategy.ContainerStrategy;
import com.genius.primavera.testcontainer.strategy.ContainerStrategyFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;

public class PrimaveraTestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        PrimaveraTestcontainersProperties properties = Binder.get(environment).bind("primavera.testcontainers", PrimaveraTestcontainersProperties.class).orElse(new PrimaveraTestcontainersProperties());
        String testClassName = getTestClassName();
        properties.getContainers().forEach((containerType, config) -> {
            if (config.isEnabled()) {
                initializeContainer(applicationContext, containerType, config, properties.getLifecycleMode(), testClassName);
            }
        });
    }

    private String getTestClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            System.out.println("  - " + className);
            if (className.contains("Test") && !className.contains("Spring") && !className.contains("junit")) {
                return className;
            }
        }
        return "UnknownTestClass";
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


    public static void stopContainers(ContainerLifecycleMode mode) {
        ContainerManager.stopContainers(mode);
    }

    public static GenericContainer<?> getContainer(String containerType, ContainerLifecycleMode mode) {
        String testClassName = getCurrentTestClassName();
        return ContainerManager.getContainer(containerType, mode, testClassName);
    }

    public static GenericContainer<?> getContainer(String containerType) {
        String testClassName = getCurrentTestClassName();
        return ContainerManager.getContainer(containerType, ContainerLifecycleMode.REUSE, testClassName);
    }

    private static String getCurrentTestClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            System.out.println("  - " + className);
            if (className.contains("Test") && !className.contains("Spring") && !className.contains("junit") && !className.contains("PrimaveraTestcontainersInitializer")) return className;
        }
        return "UnknownTestClass";
    }

    public static void stopContainers() {
        ContainerManager.stopAllContainers();
    }
}