package com.genius.primavera.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.MariaDBContainer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(MariaDBContainer.class)
@ConditionalOnProperty(name = "primavera.testcontainers.enabled", havingValue = "true", matchIfMissing = false)
@Import(TestContainerAutoConfiguration.DataSourceConfiguration.class)
public class TestContainerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "primavera.testcontainers.service.enabled", havingValue = "true", matchIfMissing = true)
    public TestContainerService testContainerService(
            @Value("${primavera.testcontainers.database-name:primavera}") String databaseName,
            @Value("${primavera.testcontainers.username:primavera}") String username,
            @Value("${primavera.testcontainers.password:primavera}") String password,
            @Value("${primavera.testcontainers.mariadb-version:mariadb:11.4.7}") String mariadbVersion,
            @Value("${primavera.testcontainers.init-script:sql/schema.sql}") String initScript) {
        return new TestContainerService(databaseName, username, password, mariadbVersion, initScript);
    }

    @Bean
    @ConditionalOnBean(TestContainerService.class)
    public MariaDBContainer<?> mariaDBContainer(TestContainerService testContainerService) {
        return testContainerService.getMariaDBContainer();
    }

    @Configuration
    @ConditionalOnBean(TestContainerService.class)
    public static class DataSourceConfiguration {

        @Bean
        @ConditionalOnBean(TestContainerService.class)
        public MapPropertySource testContainerDataSourceProperties(TestContainerService testContainerService, ConfigurableEnvironment environment) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("spring.datasource.url", testContainerService.getJdbcUrl());
            properties.put("spring.datasource.username", testContainerService.getUsername());
            properties.put("spring.datasource.password", testContainerService.getPassword());
            properties.put("spring.datasource.driver-class-name", "org.mariadb.jdbc.Driver");
            MapPropertySource propertySource = new MapPropertySource("testcontainer-datasource", properties);
            environment.getPropertySources().addFirst(propertySource);
            log.info("TestContainers DataSource 설정 완료: {}", testContainerService.getJdbcUrl());
            return propertySource;
        }

        @Bean
        @ConditionalOnBean(TestContainerService.class)
        public javax.sql.DataSource dataSource(TestContainerService testContainerService) {
            com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
            config.setJdbcUrl(testContainerService.getJdbcUrl());
            config.setUsername(testContainerService.getUsername());
            config.setPassword(testContainerService.getPassword());
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            log.info("TestContainers DataSource Bean 생성 완료: {}", testContainerService.getJdbcUrl());
            return new com.zaxxer.hikari.HikariDataSource(config);
        }
    }

}