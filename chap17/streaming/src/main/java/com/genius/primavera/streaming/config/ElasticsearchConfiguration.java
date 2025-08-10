package com.genius.primavera.streaming.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ElasticsearchConfiguration {

    @Bean
    public ElasticsearchAsyncClient elasticsearchAsyncClient(ElasticsearchClient elasticsearchClient) {
        ElasticsearchAsyncClient asyncClient = new ElasticsearchAsyncClient(elasticsearchClient._transport());
        log.info("ElasticsearchAsyncClient initialized for reactive streaming");
        return asyncClient;
    }
}