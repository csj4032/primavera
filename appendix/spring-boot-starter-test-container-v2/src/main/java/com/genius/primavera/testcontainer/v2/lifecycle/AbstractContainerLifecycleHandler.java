package com.genius.primavera.testcontainer.v2.lifecycle;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractContainerLifecycleHandler implements ContainerLifecycleHandler {
    
    protected final void processContainers(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        EnableTestContainers annotation = testClass.getAnnotation(EnableTestContainers.class);
        
        if (annotation == null) {
            log.warn("@EnableTestContainers annotation not found on test class: {}", testClass.getName());
            return;
        }
        
        log.info("Processing containers for class: {}", testClass.getName());
        
        TestContainerProperties properties = loadProperties();
        Set<ContainerType> containerTypes = Arrays.stream(annotation.containers())
                .collect(Collectors.toSet());
        ContainerLifecycleMode lifecycleMode = annotation.lifecycleMode();
        
        handleContainers(containerTypes, properties, lifecycleMode, testClass);
    }
    
    protected abstract void handleContainers(Set<ContainerType> containerTypes, 
                                           TestContainerProperties properties,
                                           ContainerLifecycleMode lifecycleMode, 
                                           Class<?> testClass);
    
    protected final TestContainerProperties loadProperties() {
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
            
            return Binder.get(environment)
                .bind("primavera.testcontainers", TestContainerProperties.class)
                .orElse(new TestContainerProperties());
            
        } catch (Exception e) {
            log.warn("Failed to load properties, using defaults", e);
            return new TestContainerProperties();
        }
    }
    
    protected final TestContainerProperties.ContainerConfig getContainerConfig(TestContainerProperties properties, ContainerType containerType) {
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
}