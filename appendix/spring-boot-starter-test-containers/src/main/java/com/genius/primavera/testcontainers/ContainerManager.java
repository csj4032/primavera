package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePropertySource;
import org.testcontainers.containers.GenericContainer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ContainerManager {

    private final EnableTestContainers annotation;
    private final Class<?> testClass;
    private final Map<String, ContainerInfo> containers = new ConcurrentHashMap<>();
    private final ContainerConfiguration configuration;
    private volatile boolean started = false;

    public ContainerManager(EnableTestContainers annotation, Class<?> testClass) {
        this.annotation = annotation;
        this.testClass = testClass;
        this.configuration = loadConfiguration();
    }

    public void startContainers() {
        if (started) {
            return;
        }

        synchronized (this) {
            if (started) {
                return;
            }

            log.info("Starting {} containers for test class: {}",
                    annotation.value().length, testClass.getSimpleName());

            List<CompletableFuture<Void>> futures = Arrays.stream(annotation.value())
                    .map(this::startContainerAsync)
                    .toList();

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(120, TimeUnit.SECONDS);
                started = true;
                log.info("All containers started successfully for test class: {}", testClass.getSimpleName());
            } catch (Exception e) {
                log.error("Failed to start containers for test class: {}", testClass.getSimpleName(), e);
                throw new RuntimeException("Container startup failed", e);
            }
        }
    }

    public void stopContainers() {
        if (!started) {
            return;
        }

        log.info("Stopping {} containers for test class: {}",
                containers.size(), testClass.getSimpleName());

        containers.values().parallelStream().forEach(containerInfo -> {
            try {
                containerInfo.container().stop();
                log.debug("Stopped container: {}", containerInfo.name());
            } catch (Exception e) {
                log.error("Failed to stop container: {}", containerInfo.name(), e);
            }
        });

        containers.clear();
        started = false;
        log.info("All containers stopped for test class: {}", testClass.getSimpleName());
    }

    public boolean isStarted() {
        return started;
    }

    public Collection<ContainerInfo> getAllContainers() {
        return Collections.unmodifiableCollection(containers.values());
    }

    public ContainerInfo getContainer(String name) {
        return containers.get(name);
    }

    private CompletableFuture<Void> startContainerAsync(EnableTestContainers.TestContainer containerDef) {
        return CompletableFuture.runAsync(() -> {
            String name = containerDef.name();
            ContainerType type = containerDef.type();

            try {
                log.info("Starting {} container: {}", type, name);

                ContainerConfiguration.ContainerSpec spec = Optional
                        .ofNullable(configuration.getContainers())
                        .map(containers -> containers.get(name))
                        .orElse(createDefaultSpec(type));

                GenericContainer<?> container = ContainerFactory.create(type, spec);
                container.start();

                ContainerInfo info = new ContainerInfo(name, type, container, spec);
                containers.put(name, info);

                log.info("Started {} container '{}' on {}:{}",
                        type, name, container.getHost(), container.getFirstMappedPort());

            } catch (Exception e) {
                log.error("Failed to start container: {}", name, e);
                throw new RuntimeException("Failed to start container: " + name, e);
            }
        });
    }

    private ContainerConfiguration loadConfiguration() {
        try {
            ConfigurableEnvironment environment = new StandardEnvironment();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            String[] yamlFiles = {"application-test.yml", "application-test.yaml"};
            for (String yamlFile : yamlFiles) {
                try {
                    Resource resource = resolver.getResource("classpath:" + yamlFile);
                    if (resource.exists()) {
                        YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();
                        List<PropertySource<?>> propertySources = yamlLoader.load(yamlFile, resource);
                        for (PropertySource<?> propertySource : propertySources) {
                            environment.getPropertySources().addFirst(propertySource);
                        }
                        log.info("Loaded YAML configuration from '{}'", yamlFile);
                        break;
                    }
                } catch (Exception e) {
                    log.warn("Failed to load YAML configuration from '{}': {}", yamlFile, e.getMessage());
                }
            }

            String[] propFiles = {"application-test.properties"};
            for (String propFile : propFiles) {
                try {
                    Resource resource = resolver.getResource("classpath:" + propFile);
                    if (resource.exists()) {
                        ResourcePropertySource source = new ResourcePropertySource(propFile, resource);
                        environment.getPropertySources().addFirst(source);
                        log.info("Loaded properties configuration from '{}'", propFile);
                        break;
                    }
                } catch (Exception e) {
                    log.warn("Failed to load properties configuration from '{}': {}", propFile, e.getMessage());
                }
            }

            return Binder.get(environment)
                    .bind("testcontainers", ContainerConfiguration.class)
                    .orElse(new ContainerConfiguration());

        } catch (Exception e) {
            log.warn("Failed to load configuration, using defaults: {}", e.getMessage());
            return new ContainerConfiguration();
        }
    }

    private ContainerConfiguration.ContainerSpec createDefaultSpec(ContainerType type) {
        return ContainerConfiguration.ContainerSpec.builder()
                .image(type.getDefaultImage())
                .build();
    }
}