package com.genius.primavera.streaming.service;

import com.genius.primavera.common.dto.ProductDocument;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Configuration;
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
public class ProductElasticsearchTemplateIntegrationTest {

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    private static final String INDEX_NAME = "product_catalog_test";
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
        assertThat(elasticsearchTemplate).isInstanceOf(DocumentOperations.class);
        assertThat(elasticsearchTemplate).isInstanceOf(SearchOperations.class);
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
                createSampleProductWithName(1L, "Smartphone Samsung Galaxy"),
                createSampleProductWithName(2L, "iPhone Apple Premium"),
                createSampleProductWithName(3L, "Laptop Gaming MSI")
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

        assertThat(searchHits.getTotalHits()).isEqualTo(1);
        SearchHit<ProductDocument> hit = searchHits.getSearchHits().get(0);
        assertThat(hit.getContent().getName()).contains("Samsung");
        assertThat(hit.getContent().getProductId()).isEqualTo(1L);
    }

    @Test
    @Order(5)
    @DisplayName("Should search products by price range using ElasticsearchTemplate")
    void shouldSearchProductsByPriceRange() {
        List<ProductDocument> products = List.of(
                createSampleProductWithPrice(1L, 50000),
                createSampleProductWithPrice(2L, 150000),
                createSampleProductWithPrice(3L, 250000)
        );

        List<IndexQuery> indexQueries = products.stream()
                .map(product -> new IndexQueryBuilder()
                        .withId(String.valueOf(product.getProductId()))
                        .withObject(product)
                        .build())
                .toList();

        elasticsearchTemplate.bulkIndex(indexQueries, INDEX_COORDINATES);
        elasticsearchTemplate.indexOps(INDEX_COORDINATES).refresh();

        String rangeQuery = """
                {
                  "range": {
                    "price": {
                      "gte": 100000,
                      "lte": 200000
                    }
                  }
                }
                """;

        Query query = new StringQuery(rangeQuery);
        SearchHits<ProductDocument> searchHits = elasticsearchTemplate.search(query, ProductDocument.class, INDEX_COORDINATES);

        assertThat(searchHits.getTotalHits()).isEqualTo(1);
        ProductDocument foundProduct = searchHits.getSearchHits().get(0).getContent();
        assertThat(foundProduct.getPrice()).isEqualTo(150000);
        assertThat(foundProduct.getProductId()).isEqualTo(2L);
    }

    @Test
    @Order(6)
    @DisplayName("Should perform complex multi-field search using ElasticsearchTemplate")
    void shouldPerformComplexMultiFieldSearch() {
        List<ProductDocument> products = List.of(
                createComplexProduct(1L, "Gaming Laptop", "High performance gaming laptop",
                        Arrays.asList("gaming", "laptop", "performance"), "Electronics", "Expensive"),
                createComplexProduct(2L, "Office Laptop", "Business laptop for productivity",
                        Arrays.asList("office", "business", "productivity"), "Electronics", "Moderate"),
                createComplexProduct(3L, "Gaming Mouse", "RGB gaming mouse with precision",
                        Arrays.asList("gaming", "mouse", "rgb", "precision"), "Accessories", "Cheap")
        );

        List<IndexQuery> indexQueries = products.stream()
                .map(product -> new IndexQueryBuilder()
                        .withId(String.valueOf(product.getProductId()))
                        .withObject(product)
                        .build())
                .toList();

        elasticsearchTemplate.bulkIndex(indexQueries, INDEX_COORDINATES);
        elasticsearchTemplate.indexOps(INDEX_COORDINATES).refresh();

        String complexQuery = """
                {
                  "bool": {
                    "must": [
                      {
                        "multi_match": {
                          "query": "gaming",
                          "fields": ["name", "description", "searchKeywords"]
                        }
                      }
                    ],
                    "filter": [
                      {
                        "term": {
                          "category.name.keyword": "Electronics"
                        }
                      }
                    ]
                  }
                }
                """;

        Query query = new StringQuery(complexQuery);
        SearchHits<ProductDocument> searchHits = elasticsearchTemplate.search(query, ProductDocument.class, INDEX_COORDINATES);

        assertThat(searchHits.getTotalHits()).isEqualTo(1);
        ProductDocument foundProduct = searchHits.getSearchHits().get(0).getContent();
        assertThat(foundProduct.getName()).isEqualTo("Gaming Laptop");
        assertThat(foundProduct.getProductId()).isEqualTo(1L);
        assertThat(foundProduct.getCategory().getName()).isEqualTo("Electronics");
    }

    @Test
    @Order(7)
    @DisplayName("Should integrate ElasticsearchTemplate with ProductSearchService for reactive operations")
    void shouldIntegrateWithProductSearchService() {
        ProductDocument product1 = createSampleProduct(1L);
        ProductDocument product2 = createSampleProduct(2L);

        IndexQuery indexQuery1 = new IndexQueryBuilder()
                .withId(String.valueOf(product1.getProductId()))
                .withObject(product1)
                .build();

        IndexQuery indexQuery2 = new IndexQueryBuilder()
                .withId(String.valueOf(product2.getProductId()))
                .withObject(product2)
                .build();

        elasticsearchTemplate.bulkIndex(List.of(indexQuery1, indexQuery2), INDEX_COORDINATES);
        elasticsearchTemplate.indexOps(INDEX_COORDINATES).refresh();

        ProductDocument newProduct = createSampleProduct(3L);

        StepVerifier.create(productSearchService.indexProduct(newProduct))
                .expectComplete()
                .verify(Duration.ofSeconds(10));

        StepVerifier.create(productSearchService.searchAllProducts())
                .expectNextCount(3)
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @Order(8)
    @DisplayName("Should perform bulk operations with reactive ProductSearchService")
    void shouldPerformBulkOperationsWithReactiveService() {
        List<ProductDocument> initialProducts = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> createSampleProduct((long) i))
                .toList();

        List<IndexQuery> indexQueries = initialProducts.stream()
                .map(product -> new IndexQueryBuilder()
                        .withId(String.valueOf(product.getProductId()))
                        .withObject(product)
                        .build())
                .toList();

        elasticsearchTemplate.bulkIndex(indexQueries, INDEX_COORDINATES);
        elasticsearchTemplate.indexOps(INDEX_COORDINATES).refresh();

        List<ProductDocument> newProducts = IntStream.rangeClosed(4, 6)
                .mapToObj(i -> createSampleProduct((long) i))
                .toList();

        Flux<ProductDocument> productFlux = Flux.fromIterable(newProducts);

        StepVerifier.create(productSearchService.bulkIndexProducts(productFlux))
                .assertNext(bulkResponse -> {
                    assertThat(bulkResponse).isNotNull();
                    assertThat(bulkResponse.errors()).isFalse();
                })
                .expectComplete()
                .verify(Duration.ofSeconds(15));

        StepVerifier.create(productSearchService.searchAllProducts())
                .expectNextCount(6)
                .expectComplete()
                .verify(Duration.ofSeconds(10));
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

    private ProductDocument createSampleProductWithPrice(Long id, Integer price) {
        return ProductDocument.builder()
                .productId(id)
                .name("Product " + id)
                .description("Product with price " + price)
                .price(price)
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
                .priceRange(price < 100000 ? "Cheap" : price < 200000 ? "Moderate" : "Expensive")
                .combinedText("Product " + id + " Product with price " + price)
                .indexedAt(Instant.now())
                .lastModified(Instant.now())
                .build();
    }

    private ProductDocument createComplexProduct(Long id, String name, String description,
                                                 List<String> keywords, String categoryName, String priceRange) {
        return ProductDocument.builder()
                .productId(id)
                .name(name)
                .description(description)
                .price(priceRange.equals("Cheap") ? 50000 : priceRange.equals("Moderate") ? 150000 : 300000)
                .status("ACTIVE")
                .seller(ProductDocument.SellerInfo.builder()
                        .id(1L)
                        .name("Test Seller")
                        .email("seller@test.com")
                        .rating(4.5)
                        .build())
                .category(ProductDocument.CategoryInfo.builder()
                        .id(1L)
                        .name(categoryName)
                        .fullPath("/" + categoryName)
                        .level(1)
                        .build())
                .searchKeywords(keywords)
                .priceRange(priceRange)
                .combinedText(name + " " + description)
                .indexedAt(Instant.now())
                .lastModified(Instant.now())
                .build();
    }

    @Configuration
    static class TestConfiguration {
    }
}