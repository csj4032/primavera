package com.genius.primavera.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ElasticsearchContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Chapter 17 Data Pipeline 통합 테스트")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static final MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:11.4.7")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("test-init.sql")
            .withReuse(true);

    @Container
    static final ElasticsearchContainer elasticsearch = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.12.0")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDB::getUsername);
        registry.add("spring.datasource.password", mariaDB::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        
        registry.add("elasticsearch.host", () -> elasticsearch.getHost());
        registry.add("elasticsearch.port", () -> elasticsearch.getMappedPort(9200));
        registry.add("elasticsearch.scheme", () -> "http");
        registry.add("elasticsearch.username", () -> "");
        registry.add("elasticsearch.password", () -> "");
        
        registry.add("debezium.enabled", () -> "false");
        
        registry.add("batch.async.enabled", () -> "false");
        registry.add("batch.thread.core-pool-size", () -> "2");
        registry.add("batch.thread.max-pool-size", () -> "4");
    }

    @BeforeEach
    void setUp() {
        if (!mariaDB.isRunning()) {
            mariaDB.start();
        }
        if (!elasticsearch.isRunning()) {
            elasticsearch.start();
        }
    }

    protected String getMariaDBJdbcUrl() {
        return mariaDB.getJdbcUrl();
    }

    protected String getElasticsearchUrl() {
        return String.format("http://%s:%d", elasticsearch.getHost(), elasticsearch.getMappedPort(9200));
    }
}