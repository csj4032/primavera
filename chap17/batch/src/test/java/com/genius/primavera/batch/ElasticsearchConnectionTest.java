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
@DisplayName("Elasticsearch 연결 테스트")
@SpringBootTest(classes = {com.genius.primavera.batch.config.ElasticsearchConfiguration.class})
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")})
public class ElasticsearchConnectionTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Elasticsearch 클라이언트가 정상적으로 주입된다")
    void shouldInjectElasticsearchClient() {
        assertNotNull(elasticsearchClient, "ElasticsearchClient가 주입되어야 한다");
        log.info("ElasticsearchClient 주입 성공: {}", elasticsearchClient.getClass().getSimpleName());
        String host = environment.getProperty("elasticsearch.host");
        String port = environment.getProperty("elasticsearch.port");
        String scheme = environment.getProperty("elasticsearch.scheme");
        log.info("Environment 속성:");
        log.info("  elasticsearch.host: {}", host);
        log.info("  elasticsearch.port: {}", port);
        log.info("  elasticsearch.scheme: {}", scheme);
        log.info("모든 TestContainer 속성 확인:");
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
    @DisplayName("Elasticsearch 클러스터 상태를 확인할 수 있다")
    void shouldCheckElasticsearchHealth() throws Exception {
        HealthResponse healthResponse = elasticsearchClient.cluster().health();
        assertNotNull(healthResponse, "Health response가 null이 아니어야 한다");
        assertNotNull(healthResponse.status(), "클러스터 상태가 있어야 한다");
        log.info("Elasticsearch 클러스터 상태: {}", healthResponse.status());
        log.info("클러스터 이름: {}", healthResponse.clusterName());
        log.info("노드 수: {}", healthResponse.numberOfNodes());
    }

    @Test
    @DisplayName("Elasticsearch 클러스터 정보를 조회할 수 있다")
    void shouldRetrieveClusterInfo() throws Exception {
        var infoResponse = elasticsearchClient.info();
        assertNotNull(infoResponse, "Info response가 null이 아니어야 한다");
        assertNotNull(infoResponse.version(), "버전 정보가 있어야 한다");
        log.info("Elasticsearch 버전: {}", infoResponse.version().number());
        log.info("클러스터 UUID: {}", infoResponse.clusterUuid());
        log.info("클러스터 이름: {}", infoResponse.clusterName());
    }
}