package com.genius.primavera.streaming.service;

import com.genius.primavera.common.dto.ProductDocument;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProductElasticsearchTemplateIntegrationTest.TestConfiguration.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ProductElasticsearchTemplate Integration Test with TestContainers")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")
})
class ProductElasticsearchTemplateIntegrationTest {

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    private static final String INDEX_NAME = "product_test_fixed";
    private static final IndexCoordinates INDEX_COORDINATES = IndexCoordinates.of(INDEX_NAME);

    @BeforeEach
    void setUp() {
        if (elasticsearchTemplate.indexOps(INDEX_COORDINATES).exists()) {
            elasticsearchTemplate.indexOps(INDEX_COORDINATES).delete();
        }
        elasticsearchTemplate.indexOps(INDEX_COORDINATES).create();
    }

    @Test
    @Order(1)
    @DisplayName("Should create ElasticsearchTemplate bean from TestContainers")
    void shouldCreateElasticsearchTemplateBean() {
        assertThat(elasticsearchTemplate).isNotNull();
        assertThat(elasticsearchTemplate.indexOps(INDEX_COORDINATES)).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Should index single product document using ElasticsearchTemplate")
    void shouldIndexSingleProductDocument() {
        ProductDocument product = createSampleProduct(1L);
        
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(String.valueOf(product.getProductId()))
                .withObject(product)
                .build();

        String documentId = elasticsearchTemplate.index(indexQuery, INDEX_COORDINATES);
        assertThat(documentId).isEqualTo("1");

        elasticsearchTemplate.indexOps(INDEX_COORDINATES).refresh();

        ProductDocument retrievedProduct = elasticsearchTemplate.get(documentId, ProductDocument.class, INDEX_COORDINATES);
        
        assertThat(retrievedProduct).isNotNull();
        assertThat(retrievedProduct.getProductId()).isEqualTo(1L);
        assertThat(retrievedProduct.getName()).isEqualTo("Test Product 1");
        assertThat(retrievedProduct.getPrice()).isEqualTo(10000);
    }

    @Test
    @Order(3)
    @DisplayName("Should bulk index multiple products using ElasticsearchTemplate")
    void shouldBulkIndexMultipleProducts() {
        List<ProductDocument> products = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> createSampleProduct((long) i))
                .toList();

        List<IndexQuery> indexQueries = products.stream()
                .map(product -> new IndexQueryBuilder()
                        .withId(String.valueOf(product.getProductId()))
                        .withObject(product)
                        .build())
                .toList();

        List<IndexedObjectInformation> indexedObjects = elasticsearchTemplate.bulkIndex(indexQueries, INDEX_COORDINATES);

        assertThat(indexedObjects).hasSize(5);
        assertThat(indexedObjects).allMatch(info -> info != null);

        elasticsearchTemplate.indexOps(INDEX_COORDINATES).refresh();

        Query query = new StringQuery("{\"match_all\": {}}");
        SearchHits<ProductDocument> searchHits = elasticsearchTemplate.search(query, ProductDocument.class, INDEX_COORDINATES);

        assertThat(searchHits.getTotalHits()).isEqualTo(5);
        assertThat(searchHits.getSearchHits()).hasSize(5);
    }

    @Test
    @Order(4)
    @DisplayName("Should search products by name using ElasticsearchTemplate")
    void shouldSearchProductsByName() {
        List<ProductDocument> products = List.of(
                createSampleProductWithName(1L, "Samsung Galaxy"),
                createSampleProductWithName(2L, "iPhone Premium"),
                createSampleProductWithName(3L, "Gaming Laptop")
        );

        List<IndexQuery> indexQueries = products.stream()
                .map(product -> new IndexQueryBuilder()
                        .withId(String.valueOf(product.getProductId()))
                        .withObject(product)
                        .build())
                .toList();

        elasticsearchTemplate.bulkIndex(indexQueries, INDEX_COORDINATES);
        elasticsearchTemplate.indexOps(INDEX_COORDINATES).refresh();

        Query query = new StringQuery("{\"match\": {\"name\": \"Samsung\"}}");
        SearchHits<ProductDocument> searchHits = elasticsearchTemplate.search(query, ProductDocument.class, INDEX_COORDINATES);

        assertThat(searchHits.getTotalHits()).isGreaterThan(0);
        if (searchHits.hasSearchHits()) {
            assertThat(searchHits.getSearchHits().get(0).getContent().getName()).contains("Samsung");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Should verify CRUD operations with ElasticsearchTemplate")
    void shouldVerifyCrudOperations() {
        ProductDocument product = createSampleProduct(999L);
        
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId("test-999")
                .withObject(product)
                .build();

        String documentId = elasticsearchTemplate.index(indexQuery, INDEX_COORDINATES);
        assertThat(documentId).isEqualTo("test-999");

        elasticsearchTemplate.indexOps(INDEX_COORDINATES).refresh();

        boolean exists = elasticsearchTemplate.exists("test-999", INDEX_COORDINATES);
        assertThat(exists).isTrue();

        elasticsearchTemplate.delete("test-999", INDEX_COORDINATES);
        elasticsearchTemplate.indexOps(INDEX_COORDINATES).refresh();
        
        boolean existsAfterDelete = elasticsearchTemplate.exists("test-999", INDEX_COORDINATES);
        assertThat(existsAfterDelete).isFalse();
    }

    private ProductDocument createSampleProduct(Long id) {
        return ProductDocument.builder()
                .productId(id)
                .name("Test Product " + id)
                .description("Test description for product " + id)
                .price(10000 * id.intValue())
                .status("ACTIVE")
                .seller(ProductDocument.SellerInfo.builder()
                        .id(1L)
                        .name("Test Seller")
                        .email("seller@test.com")
                        .rating(4.5)
                        .build())
                .category(ProductDocument.CategoryInfo.builder()
                        .id(1L)
                        .name("Electronics")
                        .fullPath("/Electronics")
                        .level(1)
                        .build())
                .searchKeywords(Arrays.asList("test", "product", "sample"))
                .priceRange("Moderate")
                .combinedText("Test Product " + id + " Test description for product " + id)
                .indexedAt(Instant.now())
                .lastModified(Instant.now())
                .build();
    }

    private ProductDocument createSampleProductWithName(Long id, String name) {
        return ProductDocument.builder()
                .productId(id)
                .name(name)
                .description("Description for " + name)
                .price(100000)
                .status("ACTIVE")
                .seller(ProductDocument.SellerInfo.builder()
                        .id(1L)
                        .name("Test Seller")
                        .email("seller@test.com")
                        .rating(4.5)
                        .build())
                .category(ProductDocument.CategoryInfo.builder()
                        .id(1L)
                        .name("Electronics")
                        .fullPath("/Electronics")
                        .level(1)
                        .build())
                .searchKeywords(Arrays.asList("test", "product"))
                .priceRange("Moderate")
                .combinedText(name + " Description for " + name)
                .indexedAt(Instant.now())
                .lastModified(Instant.now())
                .build();
    }

    @Configuration
    static class TestConfiguration {
    }
}