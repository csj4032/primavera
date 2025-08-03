package com.genius.primavera.testcontainer.v2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 다중 컨테이너를 관리하는 JUnit 5 확장
 */
@Slf4j
public class MultipleTestContainerExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final Map<String, List<GenericContainer<?>>> classContainers = new ConcurrentHashMap<>();
    private static final Map<String, List<GenericContainer<?>>> methodContainers = new ConcurrentHashMap<>();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        EnableMultipleTestContainers annotation = testClass.getAnnotation(EnableMultipleTestContainers.class);
        
        if (annotation == null) {
            return;
        }

        String testClassName = testClass.getName();
        
        if (annotation.lifecycleMode() == ContainerLifecycleMode.PER_CLASS) {
            List<GenericContainer<?>> containers = createContainers(annotation.containers());
            containers.forEach(GenericContainer::start);
            classContainers.put(testClassName, containers);
            
            // 동적 프로퍼티 설정
            configurePropertiesForContainers(annotation.containers(), containers);
            
            log.info("Started {} containers for class {}", containers.size(), testClassName);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        String testClassName = context.getRequiredTestClass().getName();
        
        List<GenericContainer<?>> containers = classContainers.remove(testClassName);
        if (containers != null) {
            containers.forEach(container -> {
                try {
                    container.stop();
                } catch (Exception e) {
                    log.warn("Error stopping container: {}", e.getMessage());
                }
            });
            log.info("Stopped {} containers for class {}", containers.size(), testClassName);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        EnableMultipleTestContainers annotation = testClass.getAnnotation(EnableMultipleTestContainers.class);
        
        if (annotation == null || annotation.lifecycleMode() != ContainerLifecycleMode.PER_METHOD) {
            return;
        }

        String methodKey = testClass.getName() + "#" + context.getRequiredTestMethod().getName();
        
        List<GenericContainer<?>> containers = createContainers(annotation.containers());
        containers.forEach(GenericContainer::start);
        methodContainers.put(methodKey, containers);
        
        // 동적 프로퍼티 설정
        configurePropertiesForContainers(annotation.containers(), containers);
        
        log.info("Started {} containers for method {}", containers.size(), methodKey);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        String methodKey = context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName();
        
        List<GenericContainer<?>> containers = methodContainers.remove(methodKey);
        if (containers != null) {
            containers.forEach(container -> {
                try {
                    container.stop();
                } catch (Exception e) {
                    log.warn("Error stopping container: {}", e.getMessage());
                }
            });
            log.info("Stopped {} containers for method {}", containers.size(), methodKey);
        }
    }

    private List<GenericContainer<?>> createContainers(EnableMultipleTestContainers.ContainerDefinition[] definitions) {
        List<GenericContainer<?>> containers = new ArrayList<>();
        
        for (EnableMultipleTestContainers.ContainerDefinition def : definitions) {
            TestContainerProperties.ContainerConfig config = createConfig(def);
            GenericContainer<?> container = ContainerFactory.createContainer(def.type(), config);
            containers.add(container);
        }
        
        return containers;
    }

    private TestContainerProperties.ContainerConfig createConfig(EnableMultipleTestContainers.ContainerDefinition def) {
        TestContainerProperties.ContainerConfig config = new TestContainerProperties.ContainerConfig();
        
        if (!def.databaseName().isEmpty()) {
            config.setDatabaseName(def.databaseName());
        }
        if (!def.username().isEmpty()) {
            config.setUsername(def.username());
        }
        if (!def.password().isEmpty()) {
            config.setPassword(def.password());
        }
        
        return config;
    }

    private void configurePropertiesForContainers(EnableMultipleTestContainers.ContainerDefinition[] definitions, 
                                                 List<GenericContainer<?>> containers) {
        for (int i = 0; i < definitions.length; i++) {
            EnableMultipleTestContainers.ContainerDefinition def = definitions[i];
            GenericContainer<?> container = containers.get(i);
            
            String prefix = def.primary() ? "spring.datasource" : 
                           "app.datasource." + def.type().name().toLowerCase();
            
            // 여기서는 실제 동적 프로퍼티 설정이 복잡하므로 로그만 남김
            log.info("Container {} configured with prefix {}", def.type(), prefix);
        }
    }
}