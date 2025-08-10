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
@DisplayName("Database to Elasticsearch translated_text_2 test")
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
            log.info("translated_text_2 translated_text_3 deletion: {}", deleteResponse.acknowledged());
        }

        Map<String, Property> properties = new HashMap<>();
        properties.put("name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
        properties.put("description", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
        properties.put("price", Property.of(p -> p.integer(i -> i)));
        properties.put("status", Property.of(p -> p.keyword(k -> k)));

        CreateIndexResponse createResponse = elasticsearchClient.indices().create(c -> c.index(INDEX_NAME).mappings(m -> m.properties(properties)));
        log.info("translated_text_3 creation: {}", createResponse.acknowledged());
    }

    @Test
    @Order(1)
    @DisplayName("translated_text_8 test translated_text_5 creation translated_text_1 exists")
    void shouldCreateTestDataInDatabase() {
        List<Category> categories = createTestCategories();
        List<Category> savedCategories = categoryRepository.saveAll(categories);
        assertEquals(3, savedCategories.size(), "3translated_text_2 translated_text_5 translated_text_5 translated_text_2");
        List<Seller> sellers = createTestSellers();
        List<Seller> savedSellers = sellerRepository.saveAll(sellers);
        assertEquals(3, savedSellers.size(), "3translated_text_2 translated_text_4 translated_text_5 translated_text_2");
        List<Product> products = createTestProducts(savedCategories, savedSellers);
        List<Product> savedProducts = productRepository.saveAll(products);
        assertEquals(TEST_DATA_COUNT, savedProducts.size(), TEST_DATA_COUNT + "translated_text_2 translated_text_3 translated_text_5 translated_text_2");
        log.info("test data creation completed:");
        log.info("- translated_text_4: {}translated_text_1", savedCategories.size());
        log.info("- translated_text_3: {}translated_text_1", savedSellers.size());
        log.info("- translated_text_2: {}translated_text_1", savedProducts.size());
    }

    @Test
    @Order(2)
    @DisplayName("Spring Batch Jobtranslated_text_2 translated_text_5 Elasticsearchtranslated_text_1 translated_text_4 translated_text_1 exists")
    void shouldIndexDataToElasticsearchUsingBatchJob() throws Exception {
        prepareTestData();
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        JobExecution jobExecution = jobLauncher.run(productIndexingJob, jobParameters);
        assertEquals("COMPLETED", jobExecution.getStatus().toString(), "Jobtranslated_text_1 translated_text_2 completed translated_text_2");
        log.info("Job execution translated_text_2: {}", jobExecution.getStatus());
        log.info("Job translated_text_2 translated_text_2: {}", jobExecution.getEndTime());
        Thread.sleep(2000);
        CountResponse countResponse = elasticsearchClient.count(c -> c.index(INDEX_NAME));
        assertEquals(TEST_DATA_COUNT, countResponse.count(), TEST_DATA_COUNT + "translated_text_2 translated_text_3 translated_text_6 translated_text_2");
        log.info("translated_text_4 translated_text_2 translated_text_1: {}", countResponse.count());
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_4 translated_text_5 Elasticsearchtranslated_text_1 translated_text_3 translated_text_1 exists")
    void shouldSearchIndexedDataFromElasticsearch() throws Exception {
        prepareTestData();
        runBatchJob();
        Thread.sleep(2000);
        SearchResponse<ProductDocument> laptopSearch = elasticsearchClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                        .multiMatch(m -> m
                                .fields("name", "description")
                                .query("translated_text_3")
                        )
                ), ProductDocument.class
        );

        assertTrue(laptopSearch.hits().total().value() > 0, "translated_text_3 translated_text_2 translated_text_3 translated_text_5 translated_text_2");
        log.info("'translated_text_3' translated_text_2 result: {}translated_text_1", laptopSearch.hits().total().value());

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

        assertTrue(priceSearch.hits().total().value() > 0, "translated_text_2 translated_text_2 translated_text_1 translated_text_3 translated_text_5 translated_text_2");
        log.info("translated_text_2 translated_text_2(50translated_text_2~100translated_text_2) translated_text_2 result: {}translated_text_1", priceSearch.hits().total().value());
        SearchResponse<ProductDocument> sellerSearch = elasticsearchClient.search(s -> s.index(INDEX_NAME).query(q -> q.match(m -> m.field("seller.name").query("Tech Store"))), ProductDocument.class);
        log.info("'Tech Store' translated_text_3 translated_text_2 result: {}translated_text_1", sellerSearch.hits().total().value());
    }

    @Test
    @Order(4)
    @DisplayName("datatranslated_text_1 Elasticsearchtranslated_text_1 data translated_text_4 translated_text_13 translated_text_1 exists")
    void shouldVerifyDataConsistency() throws Exception {
        prepareTestData();
        runBatchJob();
        Thread.sleep(2000);
        long dbCount = productRepository.count();
        CountResponse esCount = elasticsearchClient.count(c -> c.index(INDEX_NAME));
        assertEquals(dbCount, esCount.count(), "datatranslated_text_1 Elasticsearchtranslated_text_1 data translated_text_1 translated_text_4 translated_text_2");
        log.info("data translated_text_3 verification:");
        log.info("- datatranslated_text_1 translated_text_2 translated_text_1: {}", dbCount);
        log.info("- Elasticsearch translated_text_2 translated_text_1: {}", esCount.count());
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
        assertEquals(1, productSearch.hits().total().value(), "translated_text_2 translated_text_3 translated_text_3 1translated_text_1 translated_text_5 translated_text_2");
        ProductDocument document = productSearch.hits().hits().get(0).source();
        assertNotNull(document, "translated_text_3 translated_text_4 translated_text_2");
        assertEquals(firstProduct.getName(), document.getName(), "translated_text_2translated_text_1 translated_text_4 translated_text_2");
        assertEquals(firstProduct.getPrice(), document.getPrice(), "translated_text_2translated_text_1 translated_text_4 translated_text_2");
        assertEquals(firstProduct.getStatus().name(), document.getStatus(), "translated_text_2 translated_text_4 translated_text_2");
        log.info("translated_text_1 translated_text_2 validation completed - ID: {}, Name: {}", firstProduct.getId(), firstProduct.getName());
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
                .name("translated_text_3-" + timestamp)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        categories.add(Category.builder()
                .name("translated_text_4-" + timestamp)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        categories.add(Category.builder()
                .name("translated_text_3-" + timestamp)
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
            new ProductTemplate("MacBook Pro translated_text_3", "translated_text_3 translated_text_3 MacBook Pro translated_text_2 translated_text_5", 1500000, ProductStatus.ACTIVE),
            new ProductTemplate("Dell XPS translated_text_3", "translated_text_3 translated_text_3 Dell XPS translated_text_2 translated_text_5", 1700000, ProductStatus.ACTIVE),
            new ProductTemplate("ThinkPad translated_text_3", "translated_text_3 translated_text_3 ThinkPad translated_text_2 translated_text_5", 1900000, ProductStatus.ACTIVE)
        );
        
        var phoneTemplates = List.of(
            new ProductTemplate("iPhone 15 Pro Max", "translated_text_2 translated_text_4 iPhone 15 translated_text_2 translated_text_5", 1200000, ProductStatus.ACTIVE),
            new ProductTemplate("Galaxy S24 Pro Max", "translated_text_2 translated_text_4 Galaxy S24 translated_text_2 translated_text_5", 1300000, ProductStatus.ACTIVE),
            new ProductTemplate("Pixel 8 Pro Max", "translated_text_2 translated_text_4 Pixel 8 translated_text_2 translated_text_5", 1400000, ProductStatus.ACTIVE)
        );
        
        var tabletTemplates = List.of(
            new ProductTemplate("iPad Pro 2024", "translated_text_4 translated_text_3 iPad Pro translated_text_2 translated_text_5", 800000, ProductStatus.ACTIVE),
            new ProductTemplate("Galaxy Tab 2024", "translated_text_4 translated_text_3 Galaxy Tab translated_text_2 translated_text_5", 950000, ProductStatus.ACTIVE),
            new ProductTemplate("Surface Pro 2024", "translated_text_4 translated_text_3 Surface Pro translated_text_2 translated_text_5", 1100000, ProductStatus.ACTIVE),
            new ProductTemplate("Android Tablet 2024", "translated_text_4 translated_text_3 Android Tablet translated_text_2 translated_text_5", 1250000, ProductStatus.INACTIVE)
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