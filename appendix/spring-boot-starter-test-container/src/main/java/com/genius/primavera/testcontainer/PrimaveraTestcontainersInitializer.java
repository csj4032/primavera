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
        log.info("Initializing Primavera Testcontainers for test class: {}", testClassName);
        Set<ContainerType> enabledContainerTypes = getEnabledContainerTypes(testClassName);
        enabledContainerTypes.forEach(containerType -> {
            PrimaveraTestcontainersProperties.ContainerConfig config = getContainerConfig(properties, containerType);
            log.info("Container type: {}, config: {}", containerType, config);
            if (config != null) {
                initializeContainer(applicationContext, containerType, config, properties.getLifecycleMode(), testClassName);
            }
        });
    }

    private Set<ContainerType> getEnabledContainerTypes(String testClassName) {
        TestContextHolder.TestContext context = TestContextHolder.getContext();
        if (context != null && context.containerTypes() != null) {
            log.info("Found container types from ThreadLocal context: {}", context.containerTypes());
            return context.containerTypes();
        }

        String containerTypesProperty = System.getProperty("primavera.testcontainers.container-types");
        log.info("Retrieving container types from system property: primavera.testcontainers{} ", containerTypesProperty);
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
            log.info("Checking for EnablePrimaveraTestcontainers annotation on test class: {}", annotation);
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
        log.info("Retrieving test class name from ThreadLocal context: {}", context);
        if (context != null && context.testClassName() != null) return context.testClassName();
        String testClassName = System.getProperty("primavera.testcontainers.test-class");
        log.info("Retrieving test class name from system property: primavera.testcontainers.test-class = {}", testClassName);
        if (testClassName != null && !testClassName.isEmpty()) return testClassName;
        
        // 스택 트레이스 전체 출력 (디버깅용)
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        log.debug("=== Full stack trace ===");
        for (StackTraceElement element : stackTrace) {
            log.debug("  {}", element.getClassName());
        }
        log.debug("=== End stack trace ===");
        
        // 실제 테스트 클래스 찾기 - 더 정교한 로직
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            
            // 제외할 클래스들
            if (className.contains("PrimaveraTestcontainersInitializer") || 
                className.contains("TestContextHolder") ||
                className.contains("TestContainerLifecycleExtension") ||
                className.contains("DefaultTestContext") ||
                className.contains("SpringBootTestContextBootstrapper") ||
                className.contains("SpringBootContextLoader") ||
                className.contains("junit.jupiter") ||
                className.contains("org.junit") ||
                className.contains("org.springframework") ||
                className.contains("gradle")) {
                continue;
            }
            
            // @EnablePrimaveraTestcontainers 어노테이션이 있는 클래스 찾기
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(EnablePrimaveraTestcontainers.class)) {
                    log.info("Found test class with @EnablePrimaveraTestcontainers: {}", className);
                    return className;
                }
            } catch (Exception e) {
                // 클래스 로딩 실패 무시
            }
        }
        
        // fallback: Test로 끝나는 클래스 찾기
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.endsWith("Test") && !className.contains("org.") && !className.contains("junit")) {
                log.info("Found test class by name pattern: {}", className);
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
                log.info("Initialized container: {} for test class: {}", containerType, testClassName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize container: " + containerType, e);
            }
        } else {
            GenericContainer<?> existingContainer = ContainerManager.getContainer(containerType, lifecycleMode, testClassName);
            strategy.configureApplicationContext(applicationContext, existingContainer);
            log.info("Reusing existing container: {} for test class: {}", containerType, testClassName);
        }
    }
}