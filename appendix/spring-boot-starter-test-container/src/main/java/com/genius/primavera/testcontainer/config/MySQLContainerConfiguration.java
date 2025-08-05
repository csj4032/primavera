package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

/**
 * MySQL TestContainer 설정
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class MySQLContainerConfiguration {

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mySQLContainer() {
        log.info("MySQL TestContainer를 생성합니다: {}", ContainerType.MYSQL.getDefaultImage());
        
        return new MySQLContainer<>(ContainerType.MYSQL.getDefaultImage())
                .withUsername("primavera")
                .withPassword("primavera")
                .withDatabaseName("primavera")
                .withInitScript("sql/init.sql")
                .withReuse(false);
    }
}