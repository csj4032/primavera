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
@DisplayName("Elasticsearch translated_text_2 test")
@SpringBootTest(classes = {com.genius.primavera.batch.config.ElasticsearchConfiguration.class})
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")})
public class ElasticsearchConnectionTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Elasticsearch translated_text_6 successfully translated_text_4")
    void shouldInjectElasticsearchClient() {
        assertNotNull(elasticsearchClient, "ElasticsearchClienttranslated_text_1 translated_text_5 translated_text_2");
        log.info("ElasticsearchClient translated_text_2 success: {}", elasticsearchClient.getClass().getSimpleName());
        String host = environment.getProperty("elasticsearch.host");
        String port = environment.getProperty("elasticsearch.port");
        String scheme = environment.getProperty("elasticsearch.scheme");
        log.info("Environment translated_text_2:");
        log.info("  elasticsearch.host: {}", host);
        log.info("  elasticsearch.port: {}", port);
        log.info("  elasticsearch.scheme: {}", scheme);
        log.info("all TestContainer translated_text_2 verification:");
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
    @DisplayName("Elasticsearch translated_text_4 translated_text_3 verification translated_text_1 exists")
    void shouldCheckElasticsearchHealth() throws Exception {
        HealthResponse healthResponse = elasticsearchClient.cluster().health();
        assertNotNull(healthResponse, "Health responsetranslated_text_1 nulltranslated_text_1 translated_text_4 translated_text_2");
        assertNotNull(healthResponse.status(), "translated_text_4 translated_text_2translated_text_1 translated_text_3 translated_text_2");
        log.info("Elasticsearch translated_text_4 translated_text_2: {}", healthResponse.status());
        log.info("translated_text_4 translated_text_1: {}", healthResponse.clusterName());
        log.info("translated_text_2 translated_text_1: {}", healthResponse.numberOfNodes());
    }

    @Test
    @DisplayName("Elasticsearch translated_text_4 translated_text_12 translated_text_8 translated_text_1 exists")
    void shouldRetrieveClusterInfo() throws Exception {
        var infoResponse = elasticsearchClient.info();
        assertNotNull(infoResponse, "Info responsetranslated_text_1 nulltranslated_text_1 translated_text_4 translated_text_2");
        assertNotNull(infoResponse.version(), "translated_text_2 translated_text_1 translated_text_3 translated_text_2");
        log.info("Elasticsearch translated_text_2: {}", infoResponse.version().number());
        log.info("translated_text_4 UUID: {}", infoResponse.clusterUuid());
        log.info("translated_text_4 translated_text_1: {}", infoResponse.clusterName());
    }
}