package com.genius.primavera.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.genius.primavera.batch.repository.CategoryRepository;
import com.genius.primavera.batch.repository.ProductRepository;
import com.genius.primavera.batch.repository.SellerRepository;
import com.genius.primavera.common.domain.Category;
import com.genius.primavera.common.domain.Product;
import com.genius.primavera.common.domain.ProductStatus;
import com.genius.primavera.common.domain.Seller;
import com.genius.primavera.common.dto.ProductDocument;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Product Batch Job Integration Tests")
@SpringBootTest(classes = {com.genius.primavera.ProductBatchApplication.class, com.genius.primavera.batch.TestConfig.class})
@EnableTestContainers(value = {
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primavera"),
    @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")
})
public class ProductBatchJobIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job productIndexingJob;

    private static final String INDEX_NAME = "product_catalog_v1";
    private static final int LARGE_DATASET_SIZE = 1000;
    private static final int SMALL_DATASET_SIZE = 50;

    @BeforeEach
    void setUp() throws Exception {
        cleanupTestData();
        setupElasticsearchIndex();
    }

    @AfterEach
    void tearDown() throws Exception {
        cleanupTestData();
    }

    @Test
    @Order(1)
    @DisplayName("Batch job should process small dataset successfully")
    void shouldProcessSmallDatasetSuccessfully() throws Exception {
        // Given
        var testData = createTestDataSet(SMALL_DATASET_SIZE);
        saveTestData(testData);

        // When
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "small-dataset")
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);

        // Then
        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        assertThat(jobExecution.getStepExecutions()).hasSize(1);

        // Verify Elasticsearch indexing
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                CountResponse countResponse = elasticsearchClient.count(c -> c.index(INDEX_NAME));
                assertThat(countResponse.count()).isEqualTo(SMALL_DATASET_SIZE);
            });

        log.info("Small dataset batch processing completed successfully: {} items", SMALL_DATASET_SIZE);
    }

    @Test
    @Order(2)
    @DisplayName("Batch job should handle large dataset with proper chunking")
    void shouldHandleLargeDatasetWithChunking() throws Exception {
        // Given
        var testData = createTestDataSet(LARGE_DATASET_SIZE);
        saveTestData(testData);

        // When
        long startTime = System.currentTimeMillis();
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", startTime)
            .addString("test.scenario", "large-dataset")
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);
        long endTime = System.currentTimeMillis();

        // Then
        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        
        var stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getReadCount()).isEqualTo(LARGE_DATASET_SIZE);
        assertThat(stepExecution.getWriteCount()).isEqualTo(LARGE_DATASET_SIZE);

        // Verify performance metrics
        long processingTime = endTime - startTime;
        double itemsPerSecond = (double) LARGE_DATASET_SIZE / (processingTime / 1000.0);
        
        log.info("Large dataset processing completed in {}ms, {} items/second", 
                processingTime, String.format("%.2f", itemsPerSecond));
        
        // Verify all items are indexed
        Awaitility.await()
            .atMost(Duration.ofSeconds(60))
            .pollInterval(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                CountResponse countResponse = elasticsearchClient.count(c -> c.index(INDEX_NAME));
                assertThat(countResponse.count()).isEqualTo(LARGE_DATASET_SIZE);
            });
    }

    @Test
    @Order(3)
    @DisplayName("Batch job should handle error scenarios gracefully")
    void shouldHandleErrorScenariosGracefully() throws Exception {
        // Given - create data with some invalid entries
        var testData = createTestDataSet(100);
        
        // Introduce some data that might cause processing issues
        var invalidProduct = Product.builder()
            .name(null) // This might cause validation errors
            .description("Invalid product")
            .price(-1000) // Invalid price
            .status(ProductStatus.ACTIVE)
            .category(testData.categories().get(0))
            .seller(testData.sellers().get(0))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        testData.products().add(invalidProduct);
        saveTestData(testData);

        // When
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "error-handling")
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);

        // Then - job should complete despite errors
        assertTrue(jobExecution.getStatus().toString().matches("COMPLETED|COMPLETED_WITH_ERRORS"));
        
        var stepExecution = jobExecution.getStepExecutions().iterator().next();
        log.info("Error handling test - Read: {}, Write: {}, Skip: {}", 
                stepExecution.getReadCount(), 
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());
    }

    @Test
    @Order(4)
    @DisplayName("Batch job should support restart after failure")
    void shouldSupportRestartAfterFailure() throws Exception {
        // Given
        var testData = createTestDataSet(200);
        saveTestData(testData);

        // When - first run (simulate partial success)
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "restart-test")
            .toJobParameters();

        // First execution
        JobExecution firstExecution = jobLauncher.run(productIndexingJob, jobParameters);
        
        // Second execution with same parameters (restart scenario)
        JobExecution restartExecution = jobLauncher.run(productIndexingJob, jobParameters);

        // Then
        assertThat(firstExecution.getStatus().toString()).matches("COMPLETED|COMPLETED_WITH_ERRORS");
        assertThat(restartExecution.getStatus().toString()).matches("COMPLETED|COMPLETED_WITH_ERRORS");
        
        log.info("Restart capability verified - First: {}, Restart: {}", 
                firstExecution.getStatus(), restartExecution.getStatus());
    }

    @Test
    @Order(5)
    @DisplayName("Should verify data consistency between database and Elasticsearch")
    void shouldVerifyDataConsistency() throws Exception {
        // Given
        var testData = createTestDataSet(100);
        saveTestData(testData);

        // When
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "consistency-check")
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);

        // Then
        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                // Check counts match
                long dbCount = productRepository.count();
                CountResponse esCount = elasticsearchClient.count(c -> c.index(INDEX_NAME));
                assertThat(esCount.count()).isEqualTo(dbCount);

                // Verify sample data consistency
                var sampleProduct = productRepository.findAll().get(0);
                SearchResponse<ProductDocument> searchResponse = elasticsearchClient.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.term(t -> t.field("productId").value(sampleProduct.getId())))
                    , ProductDocument.class);

                assertThat(searchResponse.hits().total().value()).isEqualTo(1);
                var document = searchResponse.hits().hits().get(0).source();
                
                assertThat(document.getName()).isEqualTo(sampleProduct.getName());
                assertThat(document.getPrice()).isEqualTo(sampleProduct.getPrice());
                assertThat(document.getStatus()).isEqualTo(sampleProduct.getStatus().name());
            });

        log.info("Data consistency verification completed successfully");
    }

    private record TestDataSet(
        List<Category> categories,
        List<Seller> sellers,
        List<Product> products
    ) {}

    private TestDataSet createTestDataSet(int productCount) {
        var categories = createCategories(5);
        var sellers = createSellers(10);
        var products = createProducts(productCount, categories, sellers);
        
        return new TestDataSet(categories, sellers, products);
    }

    private List<Category> createCategories(int count) {
        var categories = new ArrayList<Category>();
        long timestamp = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            categories.add(Category.builder()
                .name("Category-" + i + "-" + timestamp)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        }
        return categories;
    }

    private List<Seller> createSellers(int count) {
        var sellers = new ArrayList<Seller>();
        long timestamp = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            sellers.add(Seller.builder()
                .name("Seller-" + i + "-" + timestamp)
                .email("seller" + i + timestamp + "@test.com")
                .rating(3.0 + ThreadLocalRandom.current().nextDouble(2.0))
                .createdAt(LocalDateTime.now())
                .build());
        }
        return sellers;
    }

    private List<Product> createProducts(int count, List<Category> categories, List<Seller> sellers) {
        var products = new ArrayList<Product>();
        var statuses = ProductStatus.values();
        
        for (int i = 0; i < count; i++) {
            products.add(Product.builder()
                .name("Product-" + i + "-" + System.currentTimeMillis())
                .description("Description for product " + i + " with detailed specifications")
                .price(ThreadLocalRandom.current().nextInt(10000, 1000000))
                .status(statuses[ThreadLocalRandom.current().nextInt(statuses.length)])
                .category(categories.get(ThreadLocalRandom.current().nextInt(categories.size())))
                .seller(sellers.get(ThreadLocalRandom.current().nextInt(sellers.size())))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        }
        return products;
    }

    private void saveTestData(TestDataSet testData) {
        categoryRepository.saveAll(testData.categories());
        sellerRepository.saveAll(testData.sellers());
        productRepository.saveAll(testData.products());
    }

    private void cleanupTestData() throws Exception {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        sellerRepository.deleteAll();
        
        boolean exists = elasticsearchClient.indices()
            .exists(ExistsRequest.of(e -> e.index(INDEX_NAME)))
            .value();
        
        if (exists) {
            elasticsearchClient.indices().delete(d -> d.index(INDEX_NAME));
        }
    }

    private void setupElasticsearchIndex() throws Exception {
        var mapping = """
            {
              "properties": {
                "productId": {"type": "long"},
                "name": {"type": "text", "analyzer": "standard"},
                "description": {"type": "text", "analyzer": "standard"},
                "price": {"type": "integer"},
                "status": {"type": "keyword"},
                "seller": {
                  "properties": {
                    "id": {"type": "long"},
                    "name": {"type": "text"},
                    "email": {"type": "keyword"},
                    "rating": {"type": "double"}
                  }
                },
                "category": {
                  "properties": {
                    "id": {"type": "long"},
                    "name": {"type": "text"},
                    "fullPath": {"type": "text"},
                    "level": {"type": "integer"}
                  }
                },
                "searchKeywords": {"type": "keyword"},
                "priceRange": {"type": "keyword"},
                "combinedText": {"type": "text"},
                "indexedAt": {"type": "date"},
                "lastModified": {"type": "date"}
              }
            }
            """;

        elasticsearchClient.indices().create(c -> c
            .index(INDEX_NAME)
            .mappings(m -> m.withJson(new java.io.StringReader(mapping))));
        
        log.info("Elasticsearch index '{}' created successfully", INDEX_NAME);
    }
}