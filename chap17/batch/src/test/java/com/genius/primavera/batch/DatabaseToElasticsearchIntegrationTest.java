package com.genius.primavera.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Database to Elasticsearch test")
@SpringBootTest(classes = {com.genius.primavera.ProductBatchApplication.class, com.genius.primavera.batch.TestConfig.class})
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primavera"), @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")})
public class DatabaseToElasticsearchIntegrationTest {

    record ProductTemplate(String name, String description, int basePrice, ProductStatus status) {}

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
    private static final int TEST_DATA_COUNT = 10;

    @BeforeEach
    void setUp() throws IOException {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        sellerRepository.deleteAll();
        boolean exists = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(INDEX_NAME))).value();
        if (exists) {
            DeleteIndexResponse deleteResponse = elasticsearchClient.indices().delete(d -> d.index(INDEX_NAME));
            log.info("test connection deletion: {}", deleteResponse.acknowledged());
        }

        Map<String, Property> properties = new HashMap<>();
        properties.put("name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
        properties.put("description", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
        properties.put("price", Property.of(p -> p.integer(i -> i)));
        properties.put("status", Property.of(p -> p.keyword(k -> k)));

        CreateIndexResponse createResponse = elasticsearchClient.indices().create(c -> c.index(INDEX_NAME).mappings(m -> m.properties(properties)));
        log.info("connection creation: {}", createResponse.acknowledged());
    }

    @Test
    @Order(1)
    @DisplayName("configuration test Endpoint creation should exists")
    void shouldCreateTestDataInDatabase() {
        List<Category> categories = createTestCategories();
        List<Category> savedCategories = categoryRepository.saveAll(categories);
        assertEquals(3, savedCategories.size(), "3test Endpoint processing test");
        List<Seller> sellers = createTestSellers();
        List<Seller> savedSellers = sellerRepository.saveAll(sellers);
        assertEquals(3, savedSellers.size(), "3test file processing test");
        List<Product> products = createTestProducts(savedCategories, savedSellers);
        List<Product> savedProducts = productRepository.saveAll(products);
        assertEquals(TEST_DATA_COUNT, savedProducts.size(), TEST_DATA_COUNT + "test connection processing test");
        log.info("test data creation completed:");
        log.info("- file: {}should", savedCategories.size());
        log.info("- connection: {}should", savedSellers.size());
        log.info("- test: {}should", savedProducts.size());
    }

    @Test
    @Order(2)
    @DisplayName("Spring Batch Jobtest Endpoint Elasticsearchshould file should exists")
    void shouldIndexDataToElasticsearchUsingBatchJob() throws Exception {
        prepareTestData();
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);
        assertEquals("COMPLETED", jobExecution.getStatus().toString(), "Jobshould test completed test");
        log.info("Job execution test: {}", jobExecution.getStatus());
        log.info("Job test: {}", jobExecution.getEndTime());
        Thread.sleep(2000);
        CountResponse countResponse = elasticsearchClient.count(c -> c.index(INDEX_NAME));
        assertEquals(TEST_DATA_COUNT, countResponse.count(), TEST_DATA_COUNT + "test connection with test");
        log.info("file test should: {}", countResponse.count());
    }

    @Test
    @Order(3)
    @DisplayName("file Endpoint Elasticsearchshould connection should exists")
    void shouldSearchIndexedDataFromElasticsearch() throws Exception {
        prepareTestData();
        runBatchJob();
        Thread.sleep(2000);
        SearchResponse<ProductDocument> laptopSearch = elasticsearchClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                        .multiMatch(m -> m
                                .fields("name", "description")
                                .query("connection")
                        )
                ), ProductDocument.class
        );

        assertTrue(laptopSearch.hits().total().value() > 0, "connection test connection processing test");
        log.info("'connection' test result: {}should", laptopSearch.hits().total().value());

        SearchResponse<ProductDocument> priceSearch = elasticsearchClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                        .range(r -> r
                                .field("price")
                                .gte(co.elastic.clients.json.JsonData.of(500000))
                                .lte(co.elastic.clients.json.JsonData.of(1000000))
                        )
                ), ProductDocument.class
        );

        assertTrue(priceSearch.hits().total().value() > 0, "test should connection processing test");
        log.info("test(50test~100test) test result: {}should", priceSearch.hits().total().value());
        SearchResponse<ProductDocument> sellerSearch = elasticsearchClient.search(s -> s.index(INDEX_NAME).query(q -> q.match(m -> m.field("seller.name").query("Tech Store"))), ProductDocument.class);
        log.info("'Tech Store' connection test result: {}should", sellerSearch.hits().total().value());
    }

    @Test
    @Order(4)
    @DisplayName("datashould Elasticsearchshould data file created successfully should exists")
    void shouldVerifyDataConsistency() throws Exception {
        prepareTestData();
        runBatchJob();
        Thread.sleep(2000);
        long dbCount = productRepository.count();
        CountResponse esCount = elasticsearchClient.count(c -> c.index(INDEX_NAME));
        assertEquals(dbCount, esCount.count(), "datashould Elasticsearchshould data should file test");
        log.info("data connection verification:");
        log.info("- datatest should: {}", dbCount);
        log.info("- Elasticsearch test should: {}", esCount.count());
        Product firstProduct = productRepository.findAll().get(0);
        SearchResponse<ProductDocument> productSearch = elasticsearchClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                        .term(t -> t
                                .field("productId")
                                .value(firstProduct.getId())
                        )
                ), ProductDocument.class
        );
        assertEquals(1, productSearch.hits().total().value(), "test connection 1should processing test");
        ProductDocument document = productSearch.hits().hits().get(0).source();
        assertNotNull(document, "connection file test");
        assertEquals(firstProduct.getName(), document.getName(), "testshould file test");
        assertEquals(firstProduct.getPrice(), document.getPrice(), "testshould file test");
        assertEquals(firstProduct.getStatus().name(), document.getStatus(), "test file test");
        log.info("should test validation completed - ID: {}, Name: {}", firstProduct.getId(), firstProduct.getName());
    }

    private void prepareTestData() {
        List<Category> categories = createTestCategories();
        List<Category> savedCategories = categoryRepository.saveAll(categories);
        List<Seller> sellers = createTestSellers();
        List<Seller> savedSellers = sellerRepository.saveAll(sellers);
        List<Product> products = createTestProducts(savedCategories, savedSellers);
        productRepository.saveAll(products);
    }

    private void runBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(productIndexingJob, jobParameters);
    }

    private List<Category> createTestCategories() {
        long timestamp = System.currentTimeMillis();
        List<Category> categories = new ArrayList<>();
        categories.add(Category.builder()
                .name("connection-" + timestamp)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        categories.add(Category.builder()
                .name("file-" + timestamp)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        categories.add(Category.builder()
                .name("connection-" + timestamp)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        return categories;
    }

    private List<Seller> createTestSellers() {
        long timestamp = System.currentTimeMillis();
        List<Seller> sellers = new ArrayList<>();
        sellers.add(Seller.builder()
                .name("Tech Store-" + timestamp)
                .email("tech" + timestamp + "@store.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build());
        sellers.add(Seller.builder()
                .name("Digital Mall-" + timestamp)
                .email("digital" + timestamp + "@mall.com")
                .rating(4.2)
                .createdAt(LocalDateTime.now())
                .build());
        sellers.add(Seller.builder()
                .name("Electronics Plus-" + timestamp)
                .email("electronics" + timestamp + "@plus.com")
                .rating(4.8)
                .createdAt(LocalDateTime.now())
                .build());
        return sellers;
    }

    private List<Product> createTestProducts(List<Category> categories, List<Seller> sellers) {
        var laptopTemplates = List.of(
            new ProductTemplate("MacBook Pro connection", "connection MacBook Pro test Endpoint", 1500000, ProductStatus.ACTIVE),
            new ProductTemplate("Dell XPS connection", "connection Dell XPS test Endpoint", 1700000, ProductStatus.ACTIVE),
            new ProductTemplate("ThinkPad connection", "connection ThinkPad test Endpoint", 1900000, ProductStatus.ACTIVE)
        );
        
        var phoneTemplates = List.of(
            new ProductTemplate("iPhone 15 Pro Max", "test file iPhone 15 test Endpoint", 1200000, ProductStatus.ACTIVE),
            new ProductTemplate("Galaxy S24 Pro Max", "test file Galaxy S24 test Endpoint", 1300000, ProductStatus.ACTIVE),
            new ProductTemplate("Pixel 8 Pro Max", "test file Pixel 8 test Endpoint", 1400000, ProductStatus.ACTIVE)
        );
        
        var tabletTemplates = List.of(
            new ProductTemplate("iPad Pro 2024", "file connection iPad Pro test Endpoint", 800000, ProductStatus.ACTIVE),
            new ProductTemplate("Galaxy Tab 2024", "file connection Galaxy Tab test Endpoint", 950000, ProductStatus.ACTIVE),
            new ProductTemplate("Surface Pro 2024", "file connection Surface Pro test Endpoint", 1100000, ProductStatus.ACTIVE),
            new ProductTemplate("Android Tablet 2024", "file connection Android Tablet test Endpoint", 1250000, ProductStatus.INACTIVE)
        );
        
        var allTemplates = List.of(
            laptopTemplates.stream().map(template -> createProduct(template, categories.getFirst(), sellers)),
            phoneTemplates.stream().map(template -> createProduct(template, categories.get(1), sellers)),
            tabletTemplates.stream().map(template -> createProduct(template, categories.get(2), sellers))
        );
        
        return allTemplates.stream()
                .flatMap(stream -> stream)
                .collect(Collectors.toList());
    }
    
    private Product createProduct(ProductTemplate template, Category category, List<Seller> sellers) {
        return Product.builder()
                .name(template.name())
                .description(template.description())
                .price(template.basePrice())
                .status(template.status())
                .category(category)
                .seller(sellers.get(ThreadLocalRandom.current().nextInt(sellers.size())))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}