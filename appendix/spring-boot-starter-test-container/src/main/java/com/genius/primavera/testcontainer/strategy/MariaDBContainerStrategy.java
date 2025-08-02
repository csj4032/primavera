package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

import java.util.HashMap;
import java.util.Map;

public class MariaDBContainerStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        PrimaveraTestcontainersProperties.DatabaseConfig dbConfig = (PrimaveraTestcontainersProperties.DatabaseConfig) config;
        
        MariaDBContainer<?> container = new MariaDBContainer<>(config.getDockerImageName())
                .withDatabaseName(dbConfig.getDatabaseName())
                .withUsername(dbConfig.getUsername())
                .withPassword(dbConfig.getPassword());

        // 초기화 스크립트가 있으면 설정
        if (dbConfig.getInitScript() != null && !dbConfig.getInitScript().isEmpty()) {
            container.withInitScript(dbConfig.getInitScript());
        }

        // 환경 변수 설정
        config.getEnvironment().forEach(container::withEnv);

        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        MariaDBContainer<?> mariadbContainer = (MariaDBContainer<?>) container;
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", mariadbContainer.getJdbcUrl());
        properties.put("spring.datasource.username", mariadbContainer.getUsername());
        properties.put("spring.datasource.password", mariadbContainer.getPassword());
        properties.put("spring.datasource.driver-class-name", mariadbContainer.getDriverClassName());

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-mariadb", properties));
    }
}