package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * PostgreSQL TestContainer 설정
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class PostgreSQLContainerConfiguration {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        log.info("PostgreSQL TestContainer를 생성합니다: {}", ContainerType.POSTGRESQL.getDefaultImage());
        
        return new PostgreSQLContainer<>(ContainerType.POSTGRESQL.getDefaultImage())
                .withUsername("primavera")
                .withPassword("primavera")
                .withDatabaseName("primavera")
                .withInitScript("sql/init-postgresql.sql")
                .withReuse(false);
    }
}