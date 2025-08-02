package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

import java.util.HashMap;
import java.util.Map;

public class MySQLContainerStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        PrimaveraTestcontainersProperties.DatabaseConfig dbConfig = (PrimaveraTestcontainersProperties.DatabaseConfig) config;
        
        MySQLContainer<?> container = new MySQLContainer<>(config.getDockerImageName())
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
        MySQLContainer<?> mysqlContainer = (MySQLContainer<?>) container;
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", mysqlContainer.getJdbcUrl());
        properties.put("spring.datasource.username", mysqlContainer.getUsername());
        properties.put("spring.datasource.password", mysqlContainer.getPassword());
        properties.put("spring.datasource.driver-class-name", mysqlContainer.getDriverClassName());

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-mysql", properties));
    }
}