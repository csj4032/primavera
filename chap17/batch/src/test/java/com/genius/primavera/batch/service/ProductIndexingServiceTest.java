package com.genius.primavera.batch.service;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.genius.primavera.batch.elasticsearch.ProductDocument;
import com.genius.primavera.common.domain.Category;
import com.genius.primavera.common.domain.Product;
import com.genius.primavera.common.domain.ProductStatus;
import com.genius.primavera.common.domain.Seller;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ProductIndexingService test")
@SpringBootTest(classes = {
    com.genius.primavera.batch.config.ElasticsearchConfiguration.class,
    com.genius.primavera.batch.service.ProductIndexingService.class
})
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")})
public class ProductIndexingServiceTest {

    @Autowired
    private ProductIndexingService productIndexingService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .id(1L)
                .name("translated_text_4")
                .level(1)
                .createdAt(LocalDateTime.now())
                .build();

        Seller seller = Seller.builder()
                .id(1L)
                .name("test translated_text_3")
                .email("seller@test.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("test translated_text_4")
                .description("translated_text_2 translated_text_3 translated_text_3 translated_text_4")
                .price(899000)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .seller(seller)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Products translated_text_4 translated_text_9 translated_text_1 exists")
    void shouldCreateProductsIndex() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        log.info("Products translated_text_3 creation test completed");
    }

    @Test
    @Order(2) 
    @DisplayName("Producttranslated_text_1 Elasticsearchtranslated_text_1 translated_text_4 translated_text_1 exists")
    void shouldIndexProduct() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        productIndexingService.indexProduct(testProduct);
        log.info("translated_text_2 translated_text_3 test completed - Product ID: {}", testProduct.getId());
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_3 Producttranslated_text_1 translated_text_3 translated_text_1 exists") 
    void shouldSearchProducts() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        productIndexingService.indexProduct(testProduct);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SearchResponse<ProductDocument> response = productIndexingService.searchProducts("translated_text_4");
        assertNotNull(response, "translated_text_2 translated_text_3 nulltranslated_text_1 translated_text_4 translated_text_2");
        assertNotNull(response.hits(), "translated_text_2 translated_text_7 translated_text_3 translated_text_2");
        assertTrue(response.hits().total().value() > 0, "translated_text_2 translated_text_3 translated_text_3 translated_text_2");
        Hit<ProductDocument> hit = response.hits().hits().get(0);
        ProductDocument document = hit.source();
        assertNotNull(document, "translated_text_2 translated_text_3 translated_text_3 translated_text_2");

        assertEquals(testProduct.getName(), document.getName(), "translated_text_2translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testProduct.getDescription(), document.getDescription(), "translated_text_2 translated_text_2translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testProduct.getPrice(), document.getPrice(), "translated_text_2 translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testProduct.getStatus().name(), document.getStatus(), "translated_text_2 translated_text_3 translated_text_4 translated_text_2");
        assertEquals(testProduct.getSeller().getName(), document.getSellerName(), "translated_text_3translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testProduct.getCategory().getName(), document.getCategoryName(), "translated_text_5translated_text_1 translated_text_4 translated_text_2");

        log.info("translated_text_2 translated_text_2 test completed - translated_text_2 translated_text_2 translated_text_1: {}", response.hits().total().value());
        log.info("translated_text_2 translated_text_2: {}", document.getName());
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_3 translated_text_3 Producttranslated_text_1 translated_text_3 translated_text_1 exists")
    void shouldSearchProductsByDifferentFields() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        productIndexingService.indexProduct(testProduct);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SearchResponse<ProductDocument> sellerResponse = productIndexingService.searchProducts("test translated_text_3");
        assertTrue(sellerResponse.hits().total().value() > 0, "translated_text_3 translated_text_2translated_text_1 translated_text_4 translated_text_2");
        SearchResponse<ProductDocument> categoryResponse = productIndexingService.searchProducts("translated_text_4");
        assertTrue(categoryResponse.hits().total().value() > 0, "translated_text_7 translated_text_2translated_text_1 translated_text_4 translated_text_2");
        SearchResponse<ProductDocument> descriptionResponse = productIndexingService.searchProducts("translated_text_2 translated_text_2");
        assertTrue(descriptionResponse.hits().total().value() > 0, "translated_text_4 translated_text_2translated_text_1 translated_text_4 translated_text_2");
        
        log.info("translated_text_2 translated_text_2 translated_text_2 test completed");
        log.info("- translated_text_3 translated_text_2: {}translated_text_1", sellerResponse.hits().total().value());
        log.info("- translated_text_5 translated_text_2: {}translated_text_1", categoryResponse.hits().total().value());
        log.info("- translated_text_2 translated_text_2: {}translated_text_1", descriptionResponse.hits().total().value());
    }
}