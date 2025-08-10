package com.genius.primavera.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.util.ObjectBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Elasticsearch connection test")
@SpringBootTest(classes = {com.genius.primavera.batch.config.ElasticsearchConfiguration.class})
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")})
public class ElasticsearchIndexTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String TEST_INDEX = "test_products";

    @Test
    @Order(1)
    @DisplayName("connection should not should exists")
    void shouldCreateIndex() throws IOException {
        Map<String, Property> properties = new HashMap<>();
        properties.put("name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
        properties.put("description", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
        properties.put("price", Property.of(p -> p.integer(i -> i)));
        CreateIndexResponse response = elasticsearchClient.indices().create(c -> c.index(TEST_INDEX).mappings(m -> m.properties(properties)));
        assertTrue(response.acknowledged(), "connection should not should not test");
        log.info("connection creation success: {}", TEST_INDEX);
        boolean exists = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(TEST_INDEX))).value();
        assertTrue(exists, "creation connectionshould file test");
        log.info("connection test verification: {}", exists);
    }

    @Test
    @Order(2)
    @DisplayName("connection file should exists")
    void shouldIndexDocument() throws IOException {
        Map<String, Object> document = new HashMap<>();
        document.put("name", "test");
        document.put("description", "connection test");
        document.put("price", 10000);
        IndexResponse response = elasticsearchClient.index(i -> i.index(TEST_INDEX).id("1").document(document));
        assertNotNull(response.id(), "test IDshould processing test");
        assertEquals("1", response.id(), "connection IDshould processing test");
        log.info("test connection success - ID: {}, Result: {}", response.id(), response.result());
    }

    @Test
    @Order(3)
    @DisplayName("connection connection should exists")
    void shouldSearchDocument() throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SearchResponse<ObjectNode> response = elasticsearchClient.search(s -> s
                .index(TEST_INDEX)
                .query(q -> q
                        .match(m -> m
                                .field("name")
                                .query("test")
                        )
                ), ObjectNode.class
        );

        assertNotNull(response.hits(), "test should connection test");
        assertTrue(response.hits().total().value() > 0, "testshould connection test");
        
        Hit<ObjectNode> hit = response.hits().hits().get(0);
        assertEquals("1", hit.id(), "test IDshould file test");
        
        ObjectNode source = hit.source();
        assertNotNull(source, "test should connection test");
        assertEquals("test", source.get("name").asText(), "test name should file test");
        assertEquals(10000, source.get("price").asInt(), "test price should file test");

        log.info("test success - should {}should test", response.hits().total().value());
        log.info("should test: {}", source.toString());
    }

    @Test
    @Order(4)
    @DisplayName("connection should not should exists")
    void shouldDeleteIndex() throws IOException {
        DeleteIndexResponse response = elasticsearchClient.indices().delete(d -> d.index(TEST_INDEX));
        assertTrue(response.acknowledged(), "connection deletionshould should not test");
        log.info("connection deletion success: {}", TEST_INDEX);
        boolean exists = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(TEST_INDEX))).value();
        assertFalse(exists, "deletion connection test connection test");
        log.info("connection deletion verification: exists = {}", exists);
    }
}