package com.genius.primavera.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.genius.primavera.batch.repository.CategoryRepository;
import com.genius.primavera.batch.repository.ProductRepository;
import com.genius.primavera.batch.repository.SellerRepository;
import com.genius.primavera.common.domain.Category;
import com.genius.primavera.common.domain.Product;
import com.genius.primavera.common.domain.ProductStatus;
import com.genius.primavera.common.domain.Seller;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Product Batch Performance Tests")
@SpringBootTest(classes = {com.genius.primavera.ProductBatchApplication.class, com.genius.primavera.batch.TestConfig.class})
@EnableTestContainers(value = {
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primavera"),
    @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")
})
public class ProductBatchPerformanceTest {

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
    @DisplayName("Performance test: 5K records batch processing")
    void shouldProcessFiveThousandRecordsEfficiently() throws Exception {
        performanceTest(5_000, "5K-records");
    }

    @Test
    @Order(2)
    @DisplayName("Performance test: 10K records batch processing")
    void shouldProcessTenThousandRecordsEfficiently() throws Exception {
        performanceTest(10_000, "10K-records");
    }

    @Test
    @Order(3)
    @DisplayName("Performance test: Chunk size optimization")
    void shouldOptimizeChunkSizePerformance() throws Exception {
        int datasetSize = 2_000;
        var testData = createTestDataSet(datasetSize);
        saveTestData(testData);

        // Test different chunk sizes through multiple runs
        String[] chunkSizes = {"50", "100", "200", "500"};
        
        for (String chunkSize : chunkSizes) {
            // Clear index before each test
            if (elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(INDEX_NAME))).value()) {
                elasticsearchClient.indices().delete(d -> d.index(INDEX_NAME));
            }
            setupElasticsearchIndex();

            long startTime = System.currentTimeMillis();
            
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("test.scenario", "chunk-size-" + chunkSize)
                .addString("chunk.size", chunkSize)
                .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);
            
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            double itemsPerSecond = (double) datasetSize / (processingTime / 1000.0);

            assertEquals("COMPLETED", jobExecution.getStatus().toString());
            
            log.info("Chunk size {} - Processing time: {}ms, Items/second: {:.2f}", 
                    chunkSize, processingTime, itemsPerSecond);
            
            // Verify all records processed
            Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    CountResponse countResponse = elasticsearchClient.count(c -> c.index(INDEX_NAME));
                    assertThat(countResponse.count()).isEqualTo(datasetSize);
                });
        }
    }

    @Test
    @Order(4)
    @DisplayName("Performance test: Memory usage monitoring")
    void shouldMonitorMemoryUsageDuringProcessing() throws Exception {
        int datasetSize = 20_000;
        var testData = createTestDataSet(datasetSize);
        saveTestData(testData);

        Runtime runtime = Runtime.getRuntime();
        
        // Measure memory before processing
        long beforeUsedMemory = runtime.totalMemory() - runtime.freeMemory();
        
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "memory-monitoring")
            .toJobParameters();

        long startTime = System.currentTimeMillis();
        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);
        long endTime = System.currentTimeMillis();

        // Measure memory after processing
        long afterUsedMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = afterUsedMemory - beforeUsedMemory;

        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        
        log.info("Memory monitoring results:");
        log.info("- Memory before: {} MB", beforeUsedMemory / (1024 * 1024));
        log.info("- Memory after: {} MB", afterUsedMemory / (1024 * 1024));
        log.info("- Memory increase: {} MB", memoryIncrease / (1024 * 1024));
        log.info("- Processing time: {}ms", endTime - startTime);
        log.info("- Records processed: {}", datasetSize);
        
        // Verify reasonable memory usage (should not exceed 500MB for 20K records)
        assertThat(memoryIncrease / (1024 * 1024)).isLessThan(500);
    }

    @Test
    @Order(5)
    @DisplayName("Performance test: Concurrent batch job execution")
    void shouldHandleConcurrentBatchJobs() throws Exception {
        int datasetSize = 1_000;
        var testData = createTestDataSet(datasetSize);
        saveTestData(testData);

        // Create multiple job executions with different parameters
        List<JobExecution> executions = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis() + i)
                .addString("test.scenario", "concurrent-" + i)
                .toJobParameters();

            JobExecution execution = jobLauncher.run(productIndexingJob, jobParameters);
            executions.add(execution);
            
            // Small delay between job starts
            Thread.sleep(100);
        }

        // Wait for all jobs to complete and verify results
        for (int i = 0; i < executions.size(); i++) {
            JobExecution execution = executions.get(i);
            
            // Jobs should complete successfully or be skipped if using same parameters
            assertThat(execution.getStatus().toString())
                .matches("COMPLETED|COMPLETED_WITH_ERRORS");
            
            log.info("Concurrent job {} completed with status: {}", i, execution.getStatus());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Performance test: Database connection pool stress test")
    void shouldHandleDatabaseConnectionPoolStress() throws Exception {
        int datasetSize = 50_000; // Large dataset to stress connection pool
        var testData = createTestDataSet(datasetSize);
        
        // Save data in batches to avoid memory issues
        saveDataInBatches(testData, 5_000);

        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", "connection-pool-stress")
            .toJobParameters();

        long startTime = System.currentTimeMillis();
        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);
        long endTime = System.currentTimeMillis();

        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        
        var stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getReadCount()).isEqualTo(datasetSize);
        assertThat(stepExecution.getWriteCount()).isEqualTo(datasetSize);

        long processingTime = endTime - startTime;
        double itemsPerSecond = (double) datasetSize / (processingTime / 1000.0);
        
        log.info("Connection pool stress test completed:");
        log.info("- Dataset size: {} records", datasetSize);
        log.info("- Processing time: {}ms", processingTime);
        log.info("- Throughput: {:.2f} items/second", itemsPerSecond);
        
        // Verify minimum performance threshold
        assertThat(itemsPerSecond).isGreaterThan(10.0); // At least 10 items per second
    }

    private void performanceTest(int datasetSize, String testName) throws Exception {
        var testData = createTestDataSet(datasetSize);
        saveTestData(testData);

        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("test.scenario", testName)
            .toJobParameters();

        long startTime = System.currentTimeMillis();
        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);
        long endTime = System.currentTimeMillis();

        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        
        var stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getReadCount()).isEqualTo(datasetSize);
        assertThat(stepExecution.getWriteCount()).isEqualTo(datasetSize);

        long processingTime = endTime - startTime;
        double itemsPerSecond = (double) datasetSize / (processingTime / 1000.0);
        
        log.info("{} Performance Results:", testName);
        log.info("- Processing time: {}ms", processingTime);
        log.info("- Throughput: {:.2f} items/second", itemsPerSecond);
        log.info("- Step read count: {}", stepExecution.getReadCount());
        log.info("- Step write count: {}", stepExecution.getWriteCount());
        
        // Performance assertions
        assertThat(processingTime).isLessThan(Duration.ofMinutes(5).toMillis());
        assertThat(itemsPerSecond).isGreaterThan(1.0);

        // Verify Elasticsearch indexing
        Awaitility.await()
            .atMost(Duration.ofSeconds(60))
            .pollInterval(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                CountResponse countResponse = elasticsearchClient.count(c -> c.index(INDEX_NAME));
                assertThat(countResponse.count()).isEqualTo(datasetSize);
            });
    }

    private record TestDataSet(
        List<Category> categories,
        List<Seller> sellers,
        List<Product> products
    ) {}

    private TestDataSet createTestDataSet(int productCount) {
        var categories = createCategories(Math.min(50, productCount / 20));
        var sellers = createSellers(Math.min(100, productCount / 10));
        var products = createProducts(productCount, categories, sellers);
        
        return new TestDataSet(categories, sellers, products);
    }

    private List<Category> createCategories(int count) {
        var categories = new ArrayList<Category>();
        long timestamp = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            categories.add(Category.builder()
                .name("PerfTestCategory-" + i + "-" + timestamp)
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
                .name("PerfTestSeller-" + i + "-" + timestamp)
                .email("perfseller" + i + timestamp + "@test.com")
                .rating(3.0 + ThreadLocalRandom.current().nextDouble(2.0))
                .createdAt(LocalDateTime.now())
                .build());
        }
        return sellers;
    }

    private List<Product> createProducts(int count, List<Category> categories, List<Seller> sellers) {
        var products = new ArrayList<Product>();
        var statuses = ProductStatus.values();
        long timestamp = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            products.add(Product.builder()
                .name("PerfTestProduct-" + i + "-" + timestamp)
                .description("Performance test product description " + i + " with detailed specifications and features")
                .price(ThreadLocalRandom.current().nextInt(1000, 100000))
                .status(statuses[ThreadLocalRandom.current().nextInt(statuses.length)])
                .category(categories.get(ThreadLocalRandom.current().nextInt(categories.size())))
                .seller(sellers.get(ThreadLocalRandom.current().nextInt(sellers.size())))
                .createdAt(LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(365)))
                .updatedAt(LocalDateTime.now())
                .build());
        }
        return products;
    }

    private void saveTestData(TestDataSet testData) {
        categoryRepository.saveAll(testData.categories());
        sellerRepository.saveAll(testData.sellers());
        productRepository.saveAll(testData.products());
        log.info("Saved test data: {} categories, {} sellers, {} products", 
                testData.categories().size(), testData.sellers().size(), testData.products().size());
    }

    private void saveDataInBatches(TestDataSet testData, int batchSize) {
        categoryRepository.saveAll(testData.categories());
        sellerRepository.saveAll(testData.sellers());
        
        // Save products in batches
        var products = testData.products();
        for (int i = 0; i < products.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, products.size());
            productRepository.saveAll(products.subList(i, endIndex));
            log.info("Saved product batch {}-{}", i, endIndex);
        }
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
                "indexedAt": {"type": "date"},
                "lastModified": {"type": "date"}
              }
            }
            """;

        elasticsearchClient.indices().create(c -> c
            .index(INDEX_NAME)
            .mappings(m -> m.withJson(new java.io.StringReader(mapping))));
    }
}