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
            log.info("translated_text_2 translated_text_3 deletion completed");
        }
    }

    @Test
    @Order(1)
    @DisplayName("translated_text_8 translated_text_5 translated_text_4 Elasticsearchtranslated_text_1 translated_text_4 translated_text_1 exists")
    void shouldSaveDataAndIndexToElasticsearch() throws Exception {

        Category category = categoryRepository.save(Category.builder()
                .name("translated_text_3")
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());

        Seller seller = sellerRepository.save(Seller.builder()
                .name("translated_text_2 translated_text_3")
                .email("tech@store.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build());

        Product product = productRepository.save(Product.builder()
                .name("MacBook Pro 16translated_text_2")
                .description("Apple M3 Pro translated_text_2 translated_text_2 translated_text_2 translated_text_2 translated_text_2")
                .price(3500000)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .seller(seller)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        log.info("translated_text_8 translated_text_2 completed:");
        log.info("- translated_text_4: {}", category.getName());
        log.info("- translated_text_3: {}", seller.getName());
        log.info("- translated_text_2: {} (ID: {})", product.getName(), product.getId());

        productIndexingService.createProductsIndexIfNotExists();

        productIndexingService.indexProduct(product);

        Thread.sleep(1000);

        SearchResponse<ProductDocument> searchResponse = productIndexingService.searchProducts("MacBook");
        
        assertNotNull(searchResponse, "translated_text_2 translated_text_3 nulltranslated_text_1 translated_text_4 translated_text_2");
        assertTrue(searchResponse.hits().total().value() > 0, "MacBook translated_text_2 translated_text_7 translated_text_3 translated_text_2");
        
        ProductDocument doc = searchResponse.hits().hits().get(0).source();
        assertNotNull(doc, "translated_text_3 translated_text_4 translated_text_2");
        assertEquals(product.getName(), doc.getName(), "translated_text_2translated_text_1 translated_text_4 translated_text_2");
        assertEquals(product.getPrice(), doc.getPrice(), "translated_text_1 translated_text_4 translated_text_2");
        assertEquals(seller.getName(), doc.getSellerName(), "translated_text_3translated_text_1 translated_text_4 translated_text_2");
        assertEquals(category.getName(), doc.getCategoryName(), "translated_text_4translated_text_1 translated_text_4 translated_text_2");
        
        log.info("Elasticsearch translated_text_3 translated_text_1 translated_text_2 success");
        log.info("translated_text_2 translated_text_2: {}", doc.getName());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 translated_text_2 translated_text_8 translated_text_4 translated_text_2 translated_text_4 translated_text_1 exists")
    void shouldBatchIndexMultipleProducts() throws Exception {

        Category laptop = categoryRepository.save(Category.builder()
                .name("translated_text_3-" + System.currentTimeMillis())
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        
        Category phone = categoryRepository.save(Category.builder()
                .name("translated_text_4-" + System.currentTimeMillis())
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());

        Seller techStore = sellerRepository.save(Seller.builder()
                .name("translated_text_2 translated_text_3-" + System.currentTimeMillis())
                .email("tech" + System.currentTimeMillis() + "@store.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build());
        
        Seller digitalMall = sellerRepository.save(Seller.builder()
                .name("translated_text_3 translated_text_1-" + System.currentTimeMillis())
                .email("digital" + System.currentTimeMillis() + "@mall.com")
                .rating(4.2)
                .createdAt(LocalDateTime.now())
                .build());

        Product macbook = productRepository.save(Product.builder()
                .name("MacBook Pro 16translated_text_2")
                .description("Apple M3 Pro translated_text_2 translated_text_2")
                .price(3500000)
                .status(ProductStatus.ACTIVE)
                .category(laptop)
                .seller(techStore)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        Product dell = productRepository.save(Product.builder()
                .name("Dell XPS 15")
                .description("translated_text_2 13translated_text_2 translated_text_2 translated_text_2")
                .price(2500000)
                .status(ProductStatus.ACTIVE)
                .category(laptop)
                .seller(digitalMall)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        Product iphone = productRepository.save(Product.builder()
                .name("iPhone 15 Pro Max")
                .description("translated_text_2 A17 Pro translated_text_2 translated_text_2")
                .price(1900000)
                .status(ProductStatus.ACTIVE)
                .category(phone)
                .seller(techStore)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        Product galaxy = productRepository.save(Product.builder()
                .name("Galaxy S24 Ultra")
                .description("translated_text_5 8 Gen 3 translated_text_2")
                .price(1800000)
                .status(ProductStatus.ACTIVE)
                .category(phone)
                .seller(digitalMall)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        log.info("translated_text_8 {}translated_text_1 translated_text_2 translated_text_2 completed", 4);

        productIndexingService.createProductsIndexIfNotExists();

        productIndexingService.indexProduct(macbook);
        productIndexingService.indexProduct(dell);
        productIndexingService.indexProduct(iphone);
        productIndexingService.indexProduct(galaxy);

        Thread.sleep(2000);

        SearchResponse<ProductDocument> laptopSearch = productIndexingService.searchProducts("translated_text_3");
        assertTrue(laptopSearch.hits().total().value() >= 2, "translated_text_3 translated_text_4 translated_text_2translated_text_1 2translated_text_1 translated_text_1 translated_text_2 translated_text_2");
        log.info("translated_text_3 translated_text_2 result: {}translated_text_1", laptopSearch.hits().total().value());
        
        SearchResponse<ProductDocument> phoneSearch = productIndexingService.searchProducts("translated_text_4");
        assertTrue(phoneSearch.hits().total().value() >= 2, "translated_text_4 translated_text_4 translated_text_2translated_text_1 2translated_text_1 translated_text_1 translated_text_2 translated_text_2");
        log.info("translated_text_4 translated_text_2 result: {}translated_text_1", phoneSearch.hits().total().value());

        SearchResponse<ProductDocument> techStoreSearch = productIndexingService.searchProducts("translated_text_2");
        assertTrue(techStoreSearch.hits().total().value() >= 2, "translated_text_2 translated_text_3 translated_text_2 translated_text_2translated_text_1 2translated_text_1 translated_text_1 translated_text_2 translated_text_2");
        log.info("translated_text_2 translated_text_3 translated_text_2 result: {}translated_text_1", techStoreSearch.hits().total().value());

        CountResponse countResponse = elasticsearchClient.count(c -> c.index(PRODUCTS_INDEX));
        assertEquals(4, countResponse.count(), "translated_text_1 4translated_text_1 translated_text_3 translated_text_3 translated_text_2");
        log.info("translated_text_2 translated_text_3 translated_text_2 translated_text_1: {}", countResponse.count());
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_8 all translated_text_2 translated_text_9 translated_text_4 translated_text_1 exists")
    @Transactional
    void shouldIndexAllProductsFromDatabase() throws Exception {

        long timestamp = System.currentTimeMillis();
        Category category = categoryRepository.save(Category.builder()
                .name("translated_text_4-" + timestamp)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        
        Seller seller = sellerRepository.save(Seller.builder()
                .name("translated_text_2 translated_text_2-" + timestamp)
                .email("general" + timestamp + "@electronics.com")
                .rating(4.3)
                .createdAt(LocalDateTime.now())
                .build());

        for (int i = 1; i <= 5; i++) {
            productRepository.save(Product.builder()
                    .name("translated_text_4 " + timestamp + "-" + i)
                    .description("translated_text_3 translated_text_4 #" + i)
                    .price(100000 * i)
                    .status(ProductStatus.ACTIVE)
                    .category(category)
                    .seller(seller)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        
        log.info("translated_text_8 5translated_text_1 translated_text_2 translated_text_2 completed");

        Iterable<Product> allProducts = productRepository.findAll();

        productIndexingService.createProductsIndexIfNotExists();

        int count = 0;
        for (Product product : allProducts) {

            product.getCategory().getName();
            product.getSeller().getName();
            productIndexingService.indexProduct(product);
            count++;
        }
        
        log.info("{}translated_text_1 translated_text_2 translated_text_3 completed", count);

        Thread.sleep(2000);

        CountResponse countResponse = elasticsearchClient.count(c -> c.index(PRODUCTS_INDEX));
        assertEquals(5, countResponse.count(), "5translated_text_1 translated_text_3 translated_text_3 translated_text_2");
        log.info("Elasticsearch translated_text_2 translated_text_1: {}", countResponse.count());

        long dbCount = productRepository.count();
        assertEquals(dbCount, countResponse.count(), "translated_text_1translated_text_1 Elasticsearch translated_text_2 translated_text_1 translated_text_4 translated_text_2");
        log.info("translated_text_1 translated_text_3 verification - DB: {}, ES: {}", dbCount, countResponse.count());
    }
}