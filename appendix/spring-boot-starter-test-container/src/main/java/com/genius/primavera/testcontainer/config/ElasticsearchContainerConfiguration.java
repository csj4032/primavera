package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * Elasticsearch TestContainer 설정
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class ElasticsearchContainerConfiguration {

    @Bean
    @ServiceConnection
    public ElasticsearchContainer elasticsearchContainer() {
        log.info("Elasticsearch TestContainer를 생성합니다: {}", ContainerType.ELASTICSEARCH.getDefaultImage());
        
        return new ElasticsearchContainer(ContainerType.ELASTICSEARCH.getDefaultImage())
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false")
                .withReuse(false);
    }
}