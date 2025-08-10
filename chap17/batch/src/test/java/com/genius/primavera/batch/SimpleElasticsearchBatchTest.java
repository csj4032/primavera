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
import com.genius.primavera.batch.service.ProductIndexingService;
import com.genius.primavera.common.domain.Category;
import com.genius.primavera.common.domain.Product;
import com.genius.primavera.common.domain.ProductStatus;
import com.genius.primavera.common.domain.Seller;
import com.genius.primavera.batch.elasticsearch.ProductDocument;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Simple Elasticsearch Batch test")
@SpringBootTest(classes = {com.genius.primavera.ProductBatchApplication.class})
@EnableTestContainers(value = {
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primavera"),
    @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")
})
public class SimpleElasticsearchBatchTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductIndexingService productIndexingService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String PRODUCTS_INDEX = "products";

    @BeforeEach
    void setUp() throws IOException {

        productRepository.deleteAll();
        categoryRepository.deleteAll();
        sellerRepository.deleteAll();

        boolean exists = elasticsearchClient.indices().exists(
            ExistsRequest.of(e -> e.index(PRODUCTS_INDEX))
        ).value();

        if (exists) {
            elasticsearchClient.indices().delete(d -> d.index(PRODUCTS_INDEX));
            log.info("test connection deletion completed");
        }
    }

    @Test
    @Order(1)
    @DisplayName("configuration Endpoint file Elasticsearchshould file should exists")
    void shouldSaveDataAndIndexToElasticsearch() throws Exception {

        Category category = categoryRepository.save(Category.builder()
                .name("connection")
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());

        Seller seller = sellerRepository.save(Seller.builder()
                .name("test connection")
                .email("tech@store.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build());

        Product product = productRepository.save(Product.builder()
                .name("MacBook Pro 16test")
                .description("Apple M3 Pro test test test")
                .price(3500000)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .seller(seller)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        log.info("configuration test completed:");
        log.info("- file: {}", category.getName());
        log.info("- connection: {}", seller.getName());
        log.info("- test: {} (ID: {})", product.getName(), product.getId());

        productIndexingService.createProductsIndexIfNotExists();

        productIndexingService.indexProduct(product);

        Thread.sleep(1000);

        SearchResponse<ProductDocument> searchResponse = productIndexingService.searchProducts("MacBook");
        
        assertNotNull(searchResponse, "test connection nullshould file test");
        assertTrue(searchResponse.hits().total().value() > 0, "MacBook test logging connection test");
        
        ProductDocument doc = searchResponse.hits().hits().get(0).source();
        assertNotNull(doc, "connection file test");
        assertEquals(product.getName(), doc.getName(), "testshould file test");
        assertEquals(product.getPrice(), doc.getPrice(), "should file test");
        assertEquals(seller.getName(), doc.getSellerName(), "connectionshould file test");
        assertEquals(category.getName(), doc.getCategoryName(), "fileshould file test");
        
        log.info("Elasticsearch connection should test success");
        log.info("test: {}", doc.getName());
    }

    @Test
    @Order(2)
    @DisplayName("test configuration file test file should exists")
    void shouldBatchIndexMultipleProducts() throws Exception {

        Category laptop = categoryRepository.save(Category.builder()
                .name("connection-" + System.currentTimeMillis())
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        
        Category phone = categoryRepository.save(Category.builder()
                .name("file-" + System.currentTimeMillis())
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());

        Seller techStore = sellerRepository.save(Seller.builder()
                .name("test connection-" + System.currentTimeMillis())
                .email("tech" + System.currentTimeMillis() + "@store.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build());
        
        Seller digitalMall = sellerRepository.save(Seller.builder()
                .name("connection should-" + System.currentTimeMillis())
                .email("digital" + System.currentTimeMillis() + "@mall.com")
                .rating(4.2)
                .createdAt(LocalDateTime.now())
                .build());

        Product macbook = productRepository.save(Product.builder()
                .name("MacBook Pro 16test")
                .description("Apple M3 Pro test")
                .price(3500000)
                .status(ProductStatus.ACTIVE)
                .category(laptop)
                .seller(techStore)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        Product dell = productRepository.save(Product.builder()
                .name("Dell XPS 15")
                .description("test 13test test")
                .price(2500000)
                .status(ProductStatus.ACTIVE)
                .category(laptop)
                .seller(digitalMall)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        Product iphone = productRepository.save(Product.builder()
                .name("iPhone 15 Pro Max")
                .description("test A17 Pro test")
                .price(1900000)
                .status(ProductStatus.ACTIVE)
                .category(phone)
                .seller(techStore)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        Product galaxy = productRepository.save(Product.builder()
                .name("Galaxy S24 Ultra")
                .description("Endpoint 8 Gen 3 test")
                .price(1800000)
                .status(ProductStatus.ACTIVE)
                .category(phone)
                .seller(digitalMall)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        log.info("configuration {}should test completed", 4);

        productIndexingService.createProductsIndexIfNotExists();

        productIndexingService.indexProduct(macbook);
        productIndexingService.indexProduct(dell);
        productIndexingService.indexProduct(iphone);
        productIndexingService.indexProduct(galaxy);

        Thread.sleep(2000);

        SearchResponse<ProductDocument> laptopSearch = productIndexingService.searchProducts("connection");
        assertTrue(laptopSearch.hits().total().value() >= 2, "connection file testshould 2needs to be added test");
        log.info("connection test result: {}should", laptopSearch.hits().total().value());
        
        SearchResponse<ProductDocument> phoneSearch = productIndexingService.searchProducts("file");
        assertTrue(phoneSearch.hits().total().value() >= 2, "file testshould 2needs to be added test");
        log.info("file test result: {}should", phoneSearch.hits().total().value());

        SearchResponse<ProductDocument> techStoreSearch = productIndexingService.searchProducts("test");
        assertTrue(techStoreSearch.hits().total().value() >= 2, "test connection testshould 2needs to be added test");
        log.info("test connection test result: {}should", techStoreSearch.hits().total().value());

        CountResponse countResponse = elasticsearchClient.count(c -> c.index(PRODUCTS_INDEX));
        assertEquals(4, countResponse.count(), "should 4should connection test");
        log.info("test connection test should: {}", countResponse.count());
    }

    @Test
    @Order(3)
    @DisplayName("configuration all test should not file should exists")
    @Transactional
    void shouldIndexAllProductsFromDatabase() throws Exception {

        long timestamp = System.currentTimeMillis();
        Category category = categoryRepository.save(Category.builder()
                .name("file-" + timestamp)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        
        Seller seller = sellerRepository.save(Seller.builder()
                .name("test-" + timestamp)
                .email("general" + timestamp + "@electronics.com")
                .rating(4.3)
                .createdAt(LocalDateTime.now())
                .build());

        for (int i = 1; i <= 5; i++) {
            productRepository.save(Product.builder()
                    .name("file " + timestamp + "-" + i)
                    .description("connection file #" + i)
                    .price(100000 * i)
                    .status(ProductStatus.ACTIVE)
                    .category(category)
                    .seller(seller)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        
        log.info("configuration 5should test completed");

        Iterable<Product> allProducts = productRepository.findAll();

        productIndexingService.createProductsIndexIfNotExists();

        int count = 0;
        for (Product product : allProducts) {

            product.getCategory().getName();
            product.getSeller().getName();
            productIndexingService.indexProduct(product);
            count++;
        }
        
        log.info("{}should test connection completed", count);

        Thread.sleep(2000);

        CountResponse countResponse = elasticsearchClient.count(c -> c.index(PRODUCTS_INDEX));
        assertEquals(5, countResponse.count(), "5should connection test");
        log.info("Elasticsearch test should: {}", countResponse.count());

        long dbCount = productRepository.count();
        assertEquals(dbCount, countResponse.count(), "shouldshould Elasticsearch test should file test");
        log.info("should connection verification - DB: {}, ES: {}", dbCount, countResponse.count());
    }
}