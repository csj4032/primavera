package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TestContainerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String PROPERTY_SOURCE_NAME = "testContainersProperties";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        ContainerManager containerManager = ContainerRegistry.get();
        if (containerManager == null) {
            log.debug("No ContainerManager found, skipping container property registration");
            return;
        }

        if (!containerManager.isStarted()) {
            log.info("Starting containers during Spring context initialization");
            containerManager.startContainers();
        }

        Map<String, Object> properties = new HashMap<>();

        ContainerInfo primaryContainer = containerManager.getAllContainers().stream()
                .filter(container -> container.getType().isSqlDatabase())
                .findFirst()
                .orElse(null);

        containerManager.getAllContainers().forEach(containerInfo -> {
            String prefix = "testcontainers.runtime." + containerInfo.getName();

            properties.put(prefix + ".host", containerInfo.getHost());
            properties.put(prefix + ".port", containerInfo.getMappedPort());
            properties.put(prefix + ".type", containerInfo.getType().name());
            properties.put(prefix + ".connection-string", containerInfo.getConnectionString());

            if (containerInfo.getType().isSqlDatabase()) {
                properties.put(prefix + ".jdbc-url", containerInfo.getJdbcUrl());
                properties.put(prefix + ".driver-class-name", containerInfo.getType().getDriverClassName());
                properties.put(prefix + ".username", containerInfo.getSpec().getUsernameOrDefault());
                properties.put(prefix + ".password", containerInfo.getSpec().getPasswordOrDefault());
                properties.put(prefix + ".database", containerInfo.getSpec().getDatabaseOrDefault());

                if (containerInfo.equals(primaryContainer)) {
                    properties.put("spring.datasource.url", containerInfo.getJdbcUrl());
                    properties.put("spring.datasource.driver-class-name", containerInfo.getType().getDriverClassName());
                    properties.put("spring.datasource.username", containerInfo.getSpec().getUsernameOrDefault());
                    properties.put("spring.datasource.password", containerInfo.getSpec().getPasswordOrDefault());

                    log.info("Configured primary DataSource for container: {} ({})",
                            containerInfo.getName(), containerInfo.getJdbcUrl());
                }
            }

            configureSpecificContainerProperties(containerInfo, properties);
        });

        if (!properties.isEmpty()) {
            MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, properties);
            environment.getPropertySources().addFirst(propertySource);

            log.info("Registered {} TestContainer runtime properties", properties.size());
        }

        applicationContext.addBeanFactoryPostProcessor(beanFactory -> {
            ContainerBeanRegistrar registrar = new ContainerBeanRegistrar(containerManager);
            registrar.registerBeans(beanFactory);
        });
    }

    private void configureSpecificContainerProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        switch (containerInfo.getType()) {
            case REDIS -> configureRedisProperties(containerInfo, properties);
            case MONGODB -> configureMongoProperties(containerInfo, properties);
            case KAFKA -> configureKafkaProperties(containerInfo, properties);
            case ELASTICSEARCH -> configureElasticsearchProperties(containerInfo, properties);
        }
    }

    private void configureRedisProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        String redisPrefix = "spring.data.redis." + containerInfo.getName();
        properties.put(redisPrefix + ".host", containerInfo.getHost());
        properties.put(redisPrefix + ".port", containerInfo.getMappedPort());

        String password = containerInfo.getSpec().getPassword();
        if (password != null && !password.isEmpty()) {
            properties.put(redisPrefix + ".password", password);
        }
    }

    private void configureMongoProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        String mongoPrefix = "spring.data.mongodb." + containerInfo.getName();
        properties.put(mongoPrefix + ".uri", containerInfo.getConnectionString());
    }

    private void configureKafkaProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        String kafkaPrefix = "spring.kafka." + containerInfo.getName();
        properties.put(kafkaPrefix + ".bootstrap-servers", containerInfo.getConnectionString());
    }

    private void configureElasticsearchProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        String esPrefix = "spring.elasticsearch." + containerInfo.getName();
        properties.put(esPrefix + ".uris", containerInfo.getConnectionString());
    }
}