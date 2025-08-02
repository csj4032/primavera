package com.genius.primavera.testcontainer;

import com.genius.primavera.testcontainer.strategy.ContainerStrategy;
import com.genius.primavera.testcontainer.strategy.ContainerStrategyFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class PrimaveraTestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("🚀 PrimaveraTestcontainersInitializer.initialize() called");
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        PrimaveraTestcontainersProperties properties = Binder.get(environment)
                .bind("primavera.testcontainers", PrimaveraTestcontainersProperties.class)
                .orElse(new PrimaveraTestcontainersProperties());
        System.out.println("📋 Properties loaded. Lifecycle mode: " + properties.getLifecycleMode());
        
        String testClassName = getTestClassName();
        System.out.println("🔍 Detected test class: " + testClassName);
        Set<ContainerType> enabledContainerTypes = getEnabledContainerTypes(testClassName);
        System.out.println("📦 Enabled container types: " + enabledContainerTypes);
        
        // 어노테이션에서 지정된 컨테이너 타입들만 처리
        enabledContainerTypes.forEach(containerType -> {
            PrimaveraTestcontainersProperties.ContainerConfig config = getContainerConfig(properties, containerType);
            if (config != null) {
                initializeContainer(applicationContext, containerType, config, properties.getLifecycleMode(), testClassName);
            }
        });
    }

    /**
     * 테스트 클래스의 EnablePrimaveraTestcontainers 어노테이션에서 지정된 컨테이너 타입들을 반환
     */
    private Set<ContainerType> getEnabledContainerTypes(String testClassName) {
        try {
            Class<?> testClass = Class.forName(testClassName);
            EnablePrimaveraTestcontainers annotation = testClass.getAnnotation(EnablePrimaveraTestcontainers.class);
            if (annotation != null) {
                return Arrays.stream(annotation.containers()).collect(Collectors.toSet());
            }
        } catch (ClassNotFoundException | SecurityException e) {
            System.out.println("Warning: Could not load test class or find annotation: " + testClassName);
        }
        
        // 기본값으로 MARIADB 반환
        return Set.of(ContainerType.MARIADB);
    }

    /**
     * ContainerType에 해당하는 설정을 반환 (application.yml 설정 우선, 없으면 기본값)
     */
    private PrimaveraTestcontainersProperties.ContainerConfig getContainerConfig(
            PrimaveraTestcontainersProperties properties, ContainerType containerType) {
        return switch (containerType) {
            case MARIADB -> properties.getMariadb();
            case MYSQL -> properties.getMysql();
            case POSTGRESQL -> properties.getPostgresql();
            case REDIS -> properties.getRedis();
            case KAFKA -> properties.getKafka();
            case ELASTICSEARCH -> properties.getElasticsearch();
        };
    }

    private String getTestClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("Test") && !className.contains("Spring") && !className.contains("junit")) {
                return className;
            }
        }
        return "UnknownTestClass";
    }

    private void initializeContainer(ConfigurableApplicationContext applicationContext, 
                                   ContainerType containerType, 
                                   PrimaveraTestcontainersProperties.ContainerConfig config, 
                                   ContainerLifecycleMode lifecycleMode, 
                                   String testClassName) {
        ContainerStrategy strategy = ContainerStrategyFactory.getStrategy(containerType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported container type: " + containerType);
        }
        
        String containerKey = containerType.name().toLowerCase();
        if (!ContainerManager.containsContainer(containerKey, lifecycleMode, testClassName)) {
            try {
                GenericContainer<?> container = strategy.createContainer(config);
                container.start();
                ContainerManager.putContainer(containerKey, container, lifecycleMode, testClassName);
                strategy.configureApplicationContext(applicationContext, container);
                System.out.println("✅ Container started: " + containerType + " for " + testClassName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize container: " + containerType, e);
            }
        } else {
            GenericContainer<?> existingContainer = ContainerManager.getContainer(containerKey, lifecycleMode, testClassName);
            strategy.configureApplicationContext(applicationContext, existingContainer);
            System.out.println("♻️ Reusing existing container: " + containerType + " for " + testClassName);
        }
    }

    public static void stopContainers() {
        ContainerManager.stopAllContainers();
    }
}