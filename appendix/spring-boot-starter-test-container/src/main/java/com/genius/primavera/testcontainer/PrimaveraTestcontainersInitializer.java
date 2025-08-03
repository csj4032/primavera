package com.genius.primavera.testContainer;

import com.genius.primavera.testContainer.strategy.ContainerStrategy;
import com.genius.primavera.testContainer.strategy.ContainerStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class PrimaveraTestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        PrimaveraTestcontainersProperties properties = Binder.get(environment).bind("primavera.testcontainers", PrimaveraTestcontainersProperties.class).orElse(new PrimaveraTestcontainersProperties());
        String testClassName = getTestClassName();
        Set<ContainerType> enabledContainerTypes = getEnabledContainerTypes(testClassName);
        enabledContainerTypes.forEach(containerType -> {
            PrimaveraTestcontainersProperties.ContainerConfig config = getContainerConfig(properties, containerType);
            if (config != null) {
                initializeContainer(applicationContext, containerType, config, properties.getLifecycleMode(), testClassName);
            }
        });
    }

    private Set<ContainerType> getEnabledContainerTypes(String testClassName) {
        TestContextHolder.TestContext context = TestContextHolder.getContext();
        log.info("Retrieving enabled container types for test class: {}", context.getContainerTypes());
        if (context.getContainerTypes() != null) {
            log.info("Found container types from ThreadLocal context: {}", context.getContainerTypes());
            return context.getContainerTypes();
        }

        String containerTypesProperty = System.getProperty("primavera.testcontainers.container-types");
        log.info("Retrieving container types from system property: primavera.testcontainers.container-types");
        if (containerTypesProperty != null && !containerTypesProperty.isEmpty()) {
            log.info("Found container types from system property: {}", containerTypesProperty);
            return Arrays.stream(containerTypesProperty.split(","))
                    .map(String::trim)
                    .map(ContainerType::valueOf)
                    .collect(Collectors.toSet());
        }

        try {
            Class<?> testClass = Class.forName(testClassName);
            EnablePrimaveraTestcontainers annotation = testClass.getAnnotation(EnablePrimaveraTestcontainers.class);
            if (annotation != null) {
                log.info("Found annotation on test class: {}", testClassName);
                return Arrays.stream(annotation.containers()).collect(Collectors.toSet());
            }
        } catch (ClassNotFoundException | SecurityException e) {
            log.warn("Failed to load test class: {} - {}", testClassName, e.getMessage());
        }

        log.info("No container configuration found, using default MARIADB");
        return Set.of(ContainerType.MARIADB);
    }

    private PrimaveraTestcontainersProperties.ContainerConfig getContainerConfig(PrimaveraTestcontainersProperties properties, ContainerType containerType) {
        return switch (containerType) {
            case MARIADB -> properties.getMariadb();
            case MYSQL -> properties.getMysql();
            case POSTGRESQL -> properties.getPostgresql();
            case REDIS -> properties.getRedis();
            case KAFKA -> properties.getKafka();
            case ELASTICSEARCH -> properties.getElasticsearch();
            case MONGODB -> properties.getMongodb();
        };
    }

    private String getTestClassName() {
        TestContextHolder.TestContext context = TestContextHolder.getContext();
        if (context != null && context.getTestClassName() != null) return context.getTestClassName();
        String testClassName = System.getProperty("primavera.testcontainers.test-class");
        if (testClassName != null && !testClassName.isEmpty()) return testClassName;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("Test") && !className.contains("Spring") && !className.contains("junit")) {
                return className;
            }
        }
        return "UnknownTestClass";
    }

    private void initializeContainer(ConfigurableApplicationContext applicationContext, ContainerType containerType, PrimaveraTestcontainersProperties.ContainerConfig config, ContainerLifecycleMode lifecycleMode, String testClassName) {
        ContainerStrategy strategy = ContainerStrategyFactory.getStrategy(containerType);
        if (!ContainerManager.containsContainer(containerType, lifecycleMode, testClassName)) {
            try {
                GenericContainer<?> container = strategy.createContainer(config);
                container.start();
                ContainerManager.putContainer(containerType, container, lifecycleMode, testClassName);
                strategy.configureApplicationContext(applicationContext, container);
                System.out.println("✅ Container started: " + containerType + " for " + testClassName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize container: " + containerType, e);
            }
        } else {
            GenericContainer<?> existingContainer = ContainerManager.getContainer(containerType, lifecycleMode, testClassName);
            strategy.configureApplicationContext(applicationContext, existingContainer);
            System.out.println("♻️ Reusing existing container: " + containerType + " for " + testClassName);
        }
    }

    public static void stopContainers() {
        ContainerManager.stopAllContainers();
    }
}