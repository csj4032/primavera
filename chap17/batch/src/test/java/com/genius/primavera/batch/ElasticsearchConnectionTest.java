package com.genius.primavera.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@DisplayName("Elasticsearch test")
@SpringBootTest(classes = {com.genius.primavera.batch.config.ElasticsearchConfiguration.class})
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")})
public class ElasticsearchConnectionTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Elasticsearch with successfully file")
    void shouldInjectElasticsearchClient() {
        assertNotNull(elasticsearchClient, "ElasticsearchClientshould processing test");
        log.info("ElasticsearchClient test success: {}", elasticsearchClient.getClass().getSimpleName());
        String host = environment.getProperty("elasticsearch.host");
        String port = environment.getProperty("elasticsearch.port");
        String scheme = environment.getProperty("elasticsearch.scheme");
        log.info("Environment test:");
        log.info("  elasticsearch.host: {}", host);
        log.info("  elasticsearch.port: {}", port);
        log.info("  elasticsearch.scheme: {}", scheme);
        log.info("all TestContainer test verification:");
        String[] tcProperties = {
            "testcontainer.runtime.elasticsearch.host",
            "testcontainer.runtime.elasticsearch.port",
            "testcontainer.runtime.elasticsearch.type",
            "testcontainer.runtime.elasticsearch.connection-string",
            "spring.elasticsearch.elasticsearch.uris"
        };
        
        for (String prop : tcProperties) {
            String value = environment.getProperty(prop);
            log.info("  {}: {}", prop, value);
        }
    }

    @Test
    @DisplayName("Elasticsearch file connection verification should exists")
    void shouldCheckElasticsearchHealth() throws Exception {
        HealthResponse healthResponse = elasticsearchClient.cluster().health();
        assertNotNull(healthResponse, "Health responseshould nullshould file test");
        assertNotNull(healthResponse.status(), "file testshould connection test");
        log.info("Elasticsearch file test: {}", healthResponse.status());
        log.info("file should: {}", healthResponse.clusterName());
        log.info("test should: {}", healthResponse.numberOfNodes());
    }

    @Test
    @DisplayName("Elasticsearch file operation configuration should exists")
    void shouldRetrieveClusterInfo() throws Exception {
        var infoResponse = elasticsearchClient.info();
        assertNotNull(infoResponse, "Info responseshould nullshould file test");
        assertNotNull(infoResponse.version(), "test should connection test");
        log.info("Elasticsearch test: {}", infoResponse.version().number());
        log.info("file UUID: {}", infoResponse.clusterUuid());
        log.info("file should: {}", infoResponse.clusterName());
    }
}