package com.genius.primavera.testcontainer.v2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 다중 컨테이너와 다중 Template을 자동으로 관리하는 확장된 Extension
 */
@Slf4j
public class MultiTemplateTestContainerExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final Map<String, List<ContainerInfo>> classContainers = new ConcurrentHashMap<>();
    private static final Map<String, List<ContainerInfo>> methodContainers = new ConcurrentHashMap<>();

    private static class ContainerInfo {
        final GenericContainer<?> container;
        final EnableMultipleTestContainers.ContainerDefinition definition;
        final String propertyPrefix;

        ContainerInfo(GenericContainer<?> container, EnableMultipleTestContainers.ContainerDefinition definition, String propertyPrefix) {
            this.container = container;
            this.definition = definition;
            this.propertyPrefix = propertyPrefix;
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        EnableMultipleTestContainers annotation = testClass.getAnnotation(EnableMultipleTestContainers.class);
        
        if (annotation == null) {
            return;
        }

        String testClassName = testClass.getName();
        
        if (annotation.lifecycleMode() == ContainerLifecycleMode.PER_CLASS) {
            List<ContainerInfo> containers = createContainers(annotation.containers());
            
            // 컨테이너 시작
            containers.forEach(info -> info.container.start());
            
            classContainers.put(testClassName, containers);
            
            // 동적 프로퍼티 설정
            configureProperties(containers);
            
            log.info("Started {} containers for class {}", containers.size(), testClassName);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        String testClassName = context.getRequiredTestClass().getName();
        
        List<ContainerInfo> containers = classContainers.remove(testClassName);
        if (containers != null) {
            containers.forEach(info -> {
                try {
                    info.container.stop();
                } catch (Exception e) {
                    log.warn("Error stopping container {}: {}", info.definition.instanceName(), e.getMessage());
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
        
        List<ContainerInfo> containers = createContainers(annotation.containers());
        
        // 컨테이너 시작
        containers.forEach(info -> info.container.start());
        
        methodContainers.put(methodKey, containers);
        
        // 동적 프로퍼티 설정
        configureProperties(containers);
        
        log.info("Started {} containers for method {}", containers.size(), methodKey);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        String methodKey = context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName();
        
        List<ContainerInfo> containers = methodContainers.remove(methodKey);
        if (containers != null) {
            containers.forEach(info -> {
                try {
                    info.container.stop();
                } catch (Exception e) {
                    log.warn("Error stopping container {}: {}", info.definition.instanceName(), e.getMessage());
                }
            });
            log.info("Stopped {} containers for method {}", containers.size(), methodKey);
        }
    }

    private List<ContainerInfo> createContainers(EnableMultipleTestContainers.ContainerDefinition[] definitions) {
        List<ContainerInfo> containers = new ArrayList<>();
        
        for (EnableMultipleTestContainers.ContainerDefinition def : definitions) {
            GenericContainer<?> container = createContainerByType(def);
            String propertyPrefix = generatePropertyPrefix(def);
            containers.add(new ContainerInfo(container, def, propertyPrefix));
        }
        
        return containers;
    }

    private GenericContainer<?> createContainerByType(EnableMultipleTestContainers.ContainerDefinition def) {
        switch (def.type()) {
            case MARIADB:
                return createMariaDBContainer(def);
            case MYSQL:
                return createMySQLContainer(def);
            case POSTGRESQL:
                return createPostgreSQLContainer(def);
            case REDIS:
                return createRedisContainer(def);
            case MONGODB:
                return createMongoDBContainer(def);
            default:
                throw new IllegalArgumentException("Unsupported container type: " + def.type());
        }
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createMariaDBContainer(EnableMultipleTestContainers.ContainerDefinition def) {
        var container = new org.testcontainers.containers.MariaDBContainer<>("mariadb:11.4.7");
        
        if (!def.databaseName().isEmpty()) {
            container.withDatabaseName(def.databaseName());
        }
        if (!def.username().isEmpty()) {
            container.withUsername(def.username());
        }
        if (!def.password().isEmpty()) {
            container.withPassword(def.password());
        }
        
        container.withInitScript("init.sql");
        
        return container;
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createMySQLContainer(EnableMultipleTestContainers.ContainerDefinition def) {
        var container = new MySQLContainer<>("mysql:8.0");
        
        if (!def.databaseName().isEmpty()) {
            container.withDatabaseName(def.databaseName());
        }
        if (!def.username().isEmpty()) {
            container.withUsername(def.username());
        }
        if (!def.password().isEmpty()) {
            container.withPassword(def.password());
        }
        
        container.withInitScript("init.sql");
        
        return container;
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createPostgreSQLContainer(EnableMultipleTestContainers.ContainerDefinition def) {
        var container = new PostgreSQLContainer<>("postgres:15");
        
        if (!def.databaseName().isEmpty()) {
            container.withDatabaseName(def.databaseName());
        }
        if (!def.username().isEmpty()) {
            container.withUsername(def.username());
        }
        if (!def.password().isEmpty()) {
            container.withPassword(def.password());
        }
        
        container.withInitScript("init.sql");
        
        return container;
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createRedisContainer(EnableMultipleTestContainers.ContainerDefinition def) {
        var container = new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379);
        
        // Redis 설정
        List<String> command = new ArrayList<>();
        command.add("redis-server");
        
        if (!def.password().isEmpty()) {
            command.add("--requirepass");
            command.add(def.password());
        }
        
        if (def.port() > 0) {
            command.add("--port");
            command.add(String.valueOf(def.port()));
        }
        
        if (command.size() > 1) {
            container.withCommand(command.toArray(new String[0]));
        }
        
        return container;
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createMongoDBContainer(EnableMultipleTestContainers.ContainerDefinition def) {
        var container = new MongoDBContainer("mongo:7.0");
        
        // MongoDB는 별도 설정이 필요한 경우 여기서 추가
        
        return container;
    }

    private String generatePropertyPrefix(EnableMultipleTestContainers.ContainerDefinition def) {
        if (def.primary()) {
            switch (def.type()) {
                case MARIADB:
                case MYSQL:
                case POSTGRESQL:
                    return "spring.datasource";
                case REDIS:
                    return "spring.data.redis";
                case MONGODB:
                    return "spring.data.mongodb";
            }
        }
        
        // Non-primary는 custom prefix 사용
        return "app." + def.instanceName().toLowerCase();
    }

    private void configureProperties(List<ContainerInfo> containers) {
        List<String> properties = new ArrayList<>();
        
        for (ContainerInfo info : containers) {
            switch (info.definition.type()) {
                case MARIADB:
                case MYSQL:
                case POSTGRESQL:
                    configureJdbcProperties(info, properties);
                    break;
                case REDIS:
                    configureRedisProperties(info, properties);
                    break;
                case MONGODB:
                    configureMongoProperties(info, properties);
                    break;
            }
        }
        
        // 실제 프로퍼티 적용은 Spring Context가 필요하므로 로그만 출력
        properties.forEach(prop -> log.info("Would configure property: {}", prop));
    }

    private void configureJdbcProperties(ContainerInfo info, List<String> properties) {
        String prefix = info.propertyPrefix;
        
        if (info.container instanceof org.testcontainers.containers.JdbcDatabaseContainer) {
            var jdbcContainer = (org.testcontainers.containers.JdbcDatabaseContainer<?>) info.container;
            
            properties.add(prefix + ".url=" + jdbcContainer.getJdbcUrl());
            properties.add(prefix + ".username=" + jdbcContainer.getUsername());
            properties.add(prefix + ".password=" + jdbcContainer.getPassword());
            properties.add(prefix + ".driver-class-name=" + jdbcContainer.getDriverClassName());
        }
    }

    private void configureRedisProperties(ContainerInfo info, List<String> properties) {
        String prefix = info.propertyPrefix;
        
        properties.add(prefix + ".host=" + info.container.getHost());
        properties.add(prefix + ".port=" + info.container.getMappedPort(6379));
        
        if (!info.definition.password().isEmpty()) {
            properties.add(prefix + ".password=" + info.definition.password());
        }
    }

    private void configureMongoProperties(ContainerInfo info, List<String> properties) {
        String prefix = info.propertyPrefix;
        
        if (info.container instanceof MongoDBContainer) {
            var mongoContainer = (MongoDBContainer) info.container;
            properties.add(prefix + ".uri=" + mongoContainer.getReplicaSetUrl());
        }
    }
}