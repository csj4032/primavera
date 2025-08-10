package com.genius.primavera.streaming.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ElasticsearchConfiguration {

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.username:elastic}")
    private String username;

    @Value("${elasticsearch.password:elastic}")
    private String password;

    @Value("${elasticsearch.scheme:http}")
    private String scheme;

    @Bean
    public RestClient elasticsearchRestClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        RestClient restClient = RestClient.builder(new HttpHost(host, port, scheme))
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
        log.info("Elasticsearch RestClient configured: {}://{}:{}", scheme, host, port);
        return restClient;
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        ElasticsearchClient client = new ElasticsearchClient(transport);
        log.info("ElasticsearchClient initialized for streaming");
        return client;
    }

    @Bean
    public ElasticsearchAsyncClient elasticsearchAsyncClient(ElasticsearchTransport transport) {
        ElasticsearchAsyncClient asyncClient = new ElasticsearchAsyncClient(transport);
        log.info("ElasticsearchAsyncClient initialized for reactive streaming");
        return asyncClient;
    }
}