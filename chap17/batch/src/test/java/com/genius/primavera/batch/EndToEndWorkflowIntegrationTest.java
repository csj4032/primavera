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
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("End-to-End Workflow Integration Tests")
@SpringBootTest(
    classes = {com.genius.primavera.ProductBatchApplication.class, com.genius.primavera.batch.TestConfig.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableTestContainers(value = {
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primavera"),
    @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")
})
public class EndToEndWorkflowIntegrationTest {

    @LocalServerPort
    private int port;

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
    private static final int WORKFLOW_DATASET_SIZE = 500;

    @BeforeEach
    void setUp() throws Exception {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        cleanupAllData();
        setupElasticsearchIndex();
    }

    @AfterEach
    void tearDown() throws Exception {
        cleanupAllData();
    }

    @Test
    @Order(1)
    @DisplayName("End-to-end workflow: Data ingestion → Batch processing → Search API")
    void shouldExecuteCompleteDataPipelineWorkflow() throws Exception {
        log.info("=== Starting End-to-End Workflow Test ===");
        
        // Step 1: Data Ingestion - Simulate data being inserted into the database
        log.info("Step 1: Ingesting test data into MariaDB...");
        var testData = createLargeTestDataSet(WORKFLOW_DATASET_SIZE);
        ingestTestDataInBatches(testData);
        
        // Verify data ingestion
        long dbProductCount = productRepository.count();
        long dbCategoryCount = categoryRepository.count();
        long dbSellerCount = sellerRepository.count();
        
        assertThat(dbProductCount).isEqualTo(WORKFLOW_DATASET_SIZE);
        assertThat(dbCategoryCount).isGreaterThan(0);
        assertThat(dbSellerCount).isGreaterThan(0);
        log.info("Step 1 completed: {} products, {} categories, {} sellers", 
                dbProductCount, dbCategoryCount, dbSellerCount);
        
        // Step 2: Batch Processing - Execute Spring Batch job to index data
        log.info("Step 2: Executing Spring Batch job for Elasticsearch indexing...");
        long batchStartTime = System.currentTimeMillis();
        
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "end-to-end-workflow")
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);
        
        long batchEndTime = System.currentTimeMillis();
        long batchDuration = batchEndTime - batchStartTime;
        
        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        log.info("Step 2 completed: Batch job finished in {}ms", batchDuration);
        
        // Step 3: Verify Elasticsearch Indexing
        log.info("Step 3: Verifying Elasticsearch indexing...");
        Awaitility.await()
            .atMost(Duration.ofSeconds(60))
            .pollInterval(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                CountResponse countResponse = elasticsearchClient.count(c -> c.index(INDEX_NAME));
                assertThat(countResponse.count()).isEqualTo(dbProductCount);
            });
        
        CountResponse finalCount = elasticsearchClient.count(c -> c.index(INDEX_NAME));
        log.info("Step 3 completed: {} documents indexed in Elasticsearch", finalCount.count());
        
        // Step 4: API Testing - Test search functionality via REST API
        log.info("Step 4: Testing search API endpoints...");
        
        // Test health check
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/actuator/health")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
        
        // Test batch job status endpoint
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/batch/jobs/status")
        .then()
            .statusCode(200);
        
        log.info("Step 4 completed: API endpoints are responsive");
        
        // Step 5: Search Functionality Testing
        log.info("Step 5: Testing search functionality...");
        
        // Test search by product name
        SearchResponse<ProductDocument> nameSearchResponse = elasticsearchClient.search(s -> s
            .index(INDEX_NAME)
            .query(q -> q.match(m -> m.field("name").query("Product")))
            .size(50)
        , ProductDocument.class);
        
        assertThat(nameSearchResponse.hits().total().value()).isGreaterThan(0);
        log.info("Name search returned {} results", nameSearchResponse.hits().total().value());
        
        // Test search by price range
        SearchResponse<ProductDocument> priceSearchResponse = elasticsearchClient.search(s -> s
            .index(INDEX_NAME)
            .query(q -> q.range(r -> r
                .field("price")
                .gte(co.elastic.clients.json.JsonData.of(10000))
                .lte(co.elastic.clients.json.JsonData.of(100000))
            ))
            .size(50)
        , ProductDocument.class);
        
        assertThat(priceSearchResponse.hits().total().value()).isGreaterThan(0);
        log.info("Price range search returned {} results", priceSearchResponse.hits().total().value());
        
        // Test search by seller
        SearchResponse<ProductDocument> sellerSearchResponse = elasticsearchClient.search(s -> s
            .index(INDEX_NAME)
            .query(q -> q.match(m -> m.field("seller.name").query("Seller")))
            .size(50)
        , ProductDocument.class);
        
        assertThat(sellerSearchResponse.hits().total().value()).isGreaterThan(0);
        log.info("Seller search returned {} results", sellerSearchResponse.hits().total().value());
        
        log.info("Step 5 completed: Search functionality verified");
        
        // Step 6: Performance Verification
        log.info("Step 6: Verifying overall performance metrics...");
        
        var stepExecution = jobExecution.getStepExecutions().iterator().next();
        double itemsPerSecond = (double) stepExecution.getWriteCount() / (batchDuration / 1000.0);
        
        assertThat(stepExecution.getReadCount()).isEqualTo(WORKFLOW_DATASET_SIZE);
        assertThat(stepExecution.getWriteCount()).isEqualTo(WORKFLOW_DATASET_SIZE);
        assertThat(itemsPerSecond).isGreaterThan(1.0); // At least 1 item per second
        
        log.info("Performance metrics:");
        log.info("- Total processing time: {}ms", batchDuration);
        log.info("- Items processed per second: {:.2f}", itemsPerSecond);
        log.info("- Read count: {}", stepExecution.getReadCount());
        log.info("- Write count: {}", stepExecution.getWriteCount());
        log.info("- Skip count: {}", stepExecution.getSkipCount());
        
        log.info("=== End-to-End Workflow Test Completed Successfully ===");
    }

    @Test
    @Order(2)
    @DisplayName("End-to-end workflow: Data updates and re-indexing")
    void shouldHandleDataUpdatesAndReIndexing() throws Exception {
        log.info("=== Starting Data Update Workflow Test ===");
        
        // Step 1: Initial data setup and indexing
        var initialData = createLargeTestDataSet(100);
        ingestTestDataInBatches(initialData);
        
        JobParameters initialJobParams = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "initial-indexing")
            .toJobParameters();
        
        JobExecution initialJobExecution = jobLauncher.run(productIndexingJob, initialJobParams);
        assertEquals("COMPLETED", initialJobExecution.getStatus().toString());
        
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                CountResponse count = elasticsearchClient.count(c -> c.index(INDEX_NAME));
                assertThat(count.count()).isEqualTo(100);
            });
        
        log.info("Step 1: Initial indexing completed with 100 products");
        
        // Step 2: Data updates - Add new products and update existing ones
        var additionalData = createLargeTestDataSet(50);
        ingestTestDataInBatches(additionalData);
        
        // Update some existing products
        var existingProducts = productRepository.findAll();
        for (int i = 0; i < Math.min(10, existingProducts.size()); i++) {
            var product = existingProducts.get(i);
            product.setName("Updated " + product.getName());
            product.setPrice(product.getPrice() + 10000);
            product.setUpdatedAt(LocalDateTime.now());
        }
        productRepository.saveAll(existingProducts.subList(0, Math.min(10, existingProducts.size())));
        
        log.info("Step 2: Added 50 new products and updated 10 existing products");
        
        // Step 3: Re-indexing with updated data
        JobParameters updateJobParams = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "update-indexing")
            .toJobParameters();
        
        JobExecution updateJobExecution = jobLauncher.run(productIndexingJob, updateJobParams);
        assertEquals("COMPLETED", updateJobExecution.getStatus().toString());
        
        // Step 4: Verify updated data in Elasticsearch
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                CountResponse count = elasticsearchClient.count(c -> c.index(INDEX_NAME));
                assertThat(count.count()).isEqualTo(150); // 100 initial + 50 new
                
                // Verify updated products are reflected in search
                SearchResponse<ProductDocument> updatedSearch = elasticsearchClient.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.match(m -> m.field("name").query("Updated")))
                , ProductDocument.class);
                
                assertThat(updatedSearch.hits().total().value()).isGreaterThanOrEqualTo(10);
            });
        
        log.info("Step 4: Re-indexing completed, verified 150 total products with updates");
        log.info("=== Data Update Workflow Test Completed Successfully ===");
    }

    @Test
    @Order(3)
    @DisplayName("End-to-end workflow: Error recovery and data consistency")
    void shouldHandleErrorRecoveryAndDataConsistency() throws Exception {
        log.info("=== Starting Error Recovery Workflow Test ===");
        
        // Step 1: Setup initial clean data
        var cleanData = createLargeTestDataSet(200);
        ingestTestDataInBatches(cleanData);
        
        // Step 2: Introduce problematic data
        var problematicProduct = Product.builder()
            .name(null) // This will cause processing issues
            .description("Problematic product")
            .price(-1000) // Invalid price
            .status(ProductStatus.ACTIVE)
            .category(cleanData.categories().get(0))
            .seller(cleanData.sellers().get(0))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        productRepository.save(problematicProduct);
        log.info("Step 2: Introduced problematic data (null name, negative price)");
        
        // Step 3: Execute batch job with error handling
        JobParameters errorJobParams = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "error-recovery")
            .toJobParameters();
        
        JobExecution errorJobExecution = jobLauncher.run(productIndexingJob, errorJobParams);
        
        // Job should complete even with errors (might be COMPLETED_WITH_ERRORS)
        assertThat(errorJobExecution.getStatus().toString())
            .matches("COMPLETED|COMPLETED_WITH_ERRORS");
        
        var errorStepExecution = errorJobExecution.getStepExecutions().iterator().next();
        log.info("Step 3: Batch job completed with status: {}", errorJobExecution.getStatus());
        log.info("Error handling stats - Read: {}, Write: {}, Skip: {}", 
                errorStepExecution.getReadCount(),
                errorStepExecution.getWriteCount(),
                errorStepExecution.getSkipCount());
        
        // Step 4: Verify data consistency despite errors
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                CountResponse count = elasticsearchClient.count(c -> c.index(INDEX_NAME));
                long dbCount = productRepository.count();
                
                // Elasticsearch count should be close to DB count (minus problematic records)
                assertThat(count.count()).isLessThanOrEqualTo(dbCount);
                assertThat(count.count()).isGreaterThan(dbCount - 10); // Allow for some error tolerance
            });
        
        CountResponse finalCount = elasticsearchClient.count(c -> c.index(INDEX_NAME));
        long finalDbCount = productRepository.count();
        
        log.info("Step 4: Data consistency verified - DB: {}, Elasticsearch: {}", 
                finalDbCount, finalCount.count());
        log.info("=== Error Recovery Workflow Test Completed Successfully ===");
    }

    private record TestDataSet(
        List<Category> categories,
        List<Seller> sellers,
        List<Product> products
    ) {}

    private TestDataSet createLargeTestDataSet(int productCount) {
        var categories = createCategories(Math.max(5, productCount / 50));
        var sellers = createSellers(Math.max(10, productCount / 20));
        var products = createProducts(productCount, categories, sellers);
        
        return new TestDataSet(categories, sellers, products);
    }

    private List<Category> createCategories(int count) {
        var categories = new ArrayList<Category>();
        long timestamp = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            categories.add(Category.builder()
                .name("WorkflowCategory-" + i + "-" + timestamp)
                .level(1 + (i % 3)) // Levels 1-3
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
                .name("WorkflowSeller-" + i + "-" + timestamp)
                .email("workflow.seller" + i + timestamp + "@test.com")
                .rating(2.0 + ThreadLocalRandom.current().nextDouble(3.0)) // Rating 2-5
                .createdAt(LocalDateTime.now())
                .build());
        }
        return sellers;
    }

    private List<Product> createProducts(int count, List<Category> categories, List<Seller> sellers) {
        var products = new ArrayList<Product>();
        var statuses = ProductStatus.values();
        long baseTimestamp = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            products.add(Product.builder()
                .name("WorkflowProduct-" + i + "-" + baseTimestamp)
                .description("End-to-end workflow test product " + i + " with comprehensive details for testing search functionality")
                .price(ThreadLocalRandom.current().nextInt(5000, 500000))
                .status(statuses[ThreadLocalRandom.current().nextInt(statuses.length)])
                .category(categories.get(ThreadLocalRandom.current().nextInt(categories.size())))
                .seller(sellers.get(ThreadLocalRandom.current().nextInt(sellers.size())))
                .createdAt(LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(30)))
                .updatedAt(LocalDateTime.now())
                .build());
        }
        return products;
    }

    private void ingestTestDataInBatches(TestDataSet testData) {
        // Save in smaller batches to avoid memory issues
        int batchSize = 100;
        
        categoryRepository.saveAll(testData.categories());
        log.info("Saved {} categories", testData.categories().size());
        
        sellerRepository.saveAll(testData.sellers());
        log.info("Saved {} sellers", testData.sellers().size());
        
        var products = testData.products();
        for (int i = 0; i < products.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, products.size());
            productRepository.saveAll(products.subList(i, endIndex));
            log.info("Saved product batch {}-{} of {}", i + 1, endIndex, products.size());
        }
    }

    private void cleanupAllData() throws Exception {
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
        
        log.info("Elasticsearch index '{}' created for workflow testing", INDEX_NAME);
    }
}