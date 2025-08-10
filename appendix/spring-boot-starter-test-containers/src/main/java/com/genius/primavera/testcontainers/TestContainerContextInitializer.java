package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.bean.BeanCreatorRegistry;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.DatabaseContainerSpec;
import com.genius.primavera.testcontainers.config.MongoContainerSpec;
import com.genius.primavera.testcontainers.config.RedisContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategyRegistry;
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
        BeanCreatorRegistry.initialize();
        
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
                .filter(container -> container.type().isSqlDatabase())
                .findFirst()
                .orElse(null);

        containerManager.getAllContainers().forEach(containerInfo -> {
            String prefix = "testcontainer.runtime." + containerInfo.name();

            properties.put(prefix + ".host", containerInfo.getHost());
            properties.put(prefix + ".port", containerInfo.getMappedPort());
            properties.put(prefix + ".type", containerInfo.type().name());
            properties.put(prefix + ".connection-string", containerInfo.getConnectionString());

            if (containerInfo.type().isSqlDatabase()) {
                properties.put(prefix + ".jdbcUrl", containerInfo.getJdbcUrl());
                properties.put(prefix + ".driver-class-name", containerInfo.type().getDriverClassName());
                
                String username = "primavera";
                String password = "primavera";
                String database = "primavera";
                
                if (containerInfo.spec() instanceof DatabaseContainerSpec dbSpec) {
                    username = dbSpec.getUsername();
                    password = dbSpec.getPassword();
                    database = dbSpec.getDatabase();
                }
                
                properties.put(prefix + ".username", username);
                properties.put(prefix + ".password", password);
                properties.put(prefix + ".database", database);

                if (containerInfo.equals(primaryContainer)) {
                    properties.put("spring.datasource.url", containerInfo.getJdbcUrl());
                    properties.put("spring.datasource.driver-class-name", containerInfo.type().getDriverClassName());
                    properties.put("spring.datasource.username", username);
                    properties.put("spring.datasource.password", password);

                    log.info("Configured primary DataSource for container: {} ({})", containerInfo.name(), containerInfo.getJdbcUrl());
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
        ContainerTypeStrategyRegistry.getStrategy(containerInfo.type())
            .ifPresent(strategy -> strategy.configureSpecificProperties(containerInfo, properties));
    }

    // Removed individual configuration methods - now handled by Strategy pattern
}