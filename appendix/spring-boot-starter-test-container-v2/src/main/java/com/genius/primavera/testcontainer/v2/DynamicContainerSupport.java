package com.genius.primavera.testcontainer.v2;

import com.genius.primavera.testcontainer.v2.configurator.DynamicPropertyConfiguratorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DynamicContainerSupport {
    
    private static final Map<String, GenericContainer<?>> staticContainers = new ConcurrentHashMap<>();
    private static TestContainerProperties cachedProperties;
    
    public static void configureContainers(Class<?> testClass, DynamicPropertyRegistry registry) {
        EnableTestContainers annotation = testClass.getAnnotation(EnableTestContainers.class);
        if (annotation == null) {
            log.warn("@EnableTestContainers annotation not found on test class: {}", testClass.getName());
            return;
        }
        
        log.info("Configuring dynamic containers for test class: {}", testClass.getName());
        TestContainerProperties properties = loadProperties();
        
        for (ContainerType containerType : annotation.containers()) {
            GenericContainer<?> container = getOrCreateContainer(containerType, properties, testClass.getName());
            configureDynamicProperties(containerType, container, registry);
        }
    }
    
    private static GenericContainer<?> getOrCreateContainer(ContainerType containerType, 
                                                           TestContainerProperties properties, 
                                                           String testClassName) {
        String containerKey = containerType + "-" + testClassName;
        
        return staticContainers.computeIfAbsent(containerKey, key -> {
            try {
                TestContainerProperties.ContainerConfig config = getContainerConfig(properties, containerType);
                GenericContainer<?> container = ContainerFactory.createContainer(containerType, config);
                
                container.start();
                log.info("Started static container: {} for {}", containerType, testClassName);
                return container;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create static container: " + containerType, e);
            }
        });
    }
    
    private static void configureDynamicProperties(ContainerType containerType, 
                                                 GenericContainer<?> container, 
                                                 DynamicPropertyRegistry registry) {
        DynamicPropertyConfiguratorFactory.configureDynamicProperties(container, registry);
    }
    
    static TestContainerProperties loadProperties() {
        if (cachedProperties != null) {
            return cachedProperties;
        }
        
        try {
            ConfigurableEnvironment environment = new StandardEnvironment();
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            String[] configFiles = {"application.yml", "application-test.yml"};
            
            for (String configFile : configFiles) {
                Resource resource = new ClassPathResource(configFile);
                if (resource.exists()) {
                    try {
                        loader.load(configFile, resource)
                              .forEach(environment.getPropertySources()::addLast);
                    } catch (Exception e) {
                        log.warn("Failed to load configuration from: {}", configFile, e);
                    }
                }
            }
            
            cachedProperties = Binder.get(environment)
                .bind("primavera.testcontainers", TestContainerProperties.class)
                .orElse(new TestContainerProperties());
            
            return cachedProperties;
        } catch (Exception e) {
            log.warn("Failed to load properties, using defaults", e);
            cachedProperties = new TestContainerProperties();
            return cachedProperties;
        }
    }
    
    private static TestContainerProperties.ContainerConfig getContainerConfig(TestContainerProperties properties, ContainerType containerType) {
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
    
    public static void stopAllContainers() {
        log.info("Stopping all static containers");
        staticContainers.values().forEach(container -> {
            try {
                container.stop();
                log.debug("Stopped container: {}", container.getDockerImageName());
            } catch (Exception e) {
                log.warn("Failed to stop container: {}", container.getDockerImageName(), e);
            }
        });
        staticContainers.clear();
    }
}