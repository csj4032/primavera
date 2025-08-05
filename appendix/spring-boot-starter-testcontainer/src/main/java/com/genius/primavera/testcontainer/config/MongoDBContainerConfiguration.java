package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;

/**
 * MongoDB TestContainer 설정
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class MongoDBContainerConfiguration {

    @Bean
    @ServiceConnection
    public MongoDBContainer mongoDBContainer() {
        log.info("MongoDB TestContainer를 생성합니다: {}", ContainerType.MONGODB.getDefaultImage());
        
        return new MongoDBContainer(ContainerType.MONGODB.getDefaultImage())
                .withReuse(false);
    }
}