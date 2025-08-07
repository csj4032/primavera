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
@DisplayName("Elasticsearch 인덱스 작업 테스트")
@SpringBootTest(classes = {com.genius.primavera.batch.config.ElasticsearchConfiguration.class})
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")})
public class ElasticsearchIndexTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String TEST_INDEX = "test_products";

    @Test
    @Order(1)
    @DisplayName("인덱스를 생성할 수 있다")
    void shouldCreateIndex() throws IOException {
        Map<String, Property> properties = new HashMap<>();
        properties.put("name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
        properties.put("description", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
        properties.put("price", Property.of(p -> p.integer(i -> i)));
        CreateIndexResponse response = elasticsearchClient.indices().create(c -> c.index(TEST_INDEX).mappings(m -> m.properties(properties)));
        assertTrue(response.acknowledged(), "인덱스 생성이 성공해야 한다");
        log.info("인덱스 생성 성공: {}", TEST_INDEX);
        boolean exists = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(TEST_INDEX))).value();
        assertTrue(exists, "생성된 인덱스가 존재해야 한다");
        log.info("인덱스 존재 확인: {}", exists);
    }

    @Test
    @Order(2)
    @DisplayName("문서를 인덱싱할 수 있다")
    void shouldIndexDocument() throws IOException {
        Map<String, Object> document = new HashMap<>();
        document.put("name", "테스트 상품");
        document.put("description", "이것은 테스트용 상품입니다");
        document.put("price", 10000);
        IndexResponse response = elasticsearchClient.index(i -> i.index(TEST_INDEX).id("1").document(document));
        assertNotNull(response.id(), "문서 ID가 반환되어야 한다");
        assertEquals("1", response.id(), "지정한 ID가 반환되어야 한다");
        log.info("문서 인덱싱 성공 - ID: {}, Result: {}", response.id(), response.result());
    }

    @Test
    @Order(3)
    @DisplayName("인덱싱된 문서를 검색할 수 있다")
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
                                .query("테스트")
                        )
                ), ObjectNode.class
        );

        assertNotNull(response.hits(), "검색 결과가 있어야 한다");
        assertTrue(response.hits().total().value() > 0, "검색된 문서가 있어야 한다");
        
        Hit<ObjectNode> hit = response.hits().hits().get(0);
        assertEquals("1", hit.id(), "검색된 문서의 ID가 일치해야 한다");
        
        ObjectNode source = hit.source();
        assertNotNull(source, "문서 소스가 있어야 한다");
        assertEquals("테스트 상품", source.get("name").asText(), "문서의 name 필드가 일치해야 한다");
        assertEquals(10000, source.get("price").asInt(), "문서의 price 필드가 일치해야 한다");

        log.info("검색 성공 - 총 {}개 문서 발견", response.hits().total().value());
        log.info("첫 번째 문서: {}", source.toString());
    }

    @Test
    @Order(4)
    @DisplayName("인덱스를 삭제할 수 있다")
    void shouldDeleteIndex() throws IOException {
        DeleteIndexResponse response = elasticsearchClient.indices().delete(d -> d.index(TEST_INDEX));
        assertTrue(response.acknowledged(), "인덱스 삭제가 성공해야 한다");
        log.info("인덱스 삭제 성공: {}", TEST_INDEX);
        boolean exists = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(TEST_INDEX))).value();
        assertFalse(exists, "삭제된 인덱스는 존재하지 않아야 한다");
        log.info("인덱스 삭제 확인: exists = {}", exists);
    }
}