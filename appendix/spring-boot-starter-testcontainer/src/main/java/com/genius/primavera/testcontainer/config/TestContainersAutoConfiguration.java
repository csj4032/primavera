package com.genius.primavera.testcontainer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MariaDBContainer;

@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersAutoConfiguration {

    @Bean
    @ServiceConnection
    public MariaDBContainer<?> postgresContainer() {
        return new MariaDBContainer<>("mariadb:10.5")
                .withUsername("primavera")
                .withPassword("primavera")
                .withDatabaseName("primavera")
                .withInitScript("sql/init.sql");
    }
}