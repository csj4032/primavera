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
                .name("file")
                .level(1)
                .createdAt(LocalDateTime.now())
                .build();

        Seller seller = Seller.builder()
                .id(1L)
                .name("test connection")
                .email("seller@test.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("test file")
                .description("test connection file")
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
    @DisplayName("Products file should not should exists")
    void shouldCreateProductsIndex() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        log.info("Products connection creation test completed");
    }

    @Test
    @Order(2) 
    @DisplayName("Productshould Elasticsearchshould file should exists")
    void shouldIndexProduct() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        productIndexingService.indexProduct(testProduct);
        log.info("test connection test completed - Product ID: {}", testProduct.getId());
    }

    @Test
    @Order(3)
    @DisplayName("connection Productshould connection should exists") 
    void shouldSearchProducts() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        productIndexingService.indexProduct(testProduct);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SearchResponse<ProductDocument> response = productIndexingService.searchProducts("file");
        assertNotNull(response, "test connection nullshould file test");
        assertNotNull(response.hits(), "test logging connection test");
        assertTrue(response.hits().total().value() > 0, "test connection test");
        Hit<ProductDocument> hit = response.hits().hits().get(0);
        ProductDocument document = hit.source();
        assertNotNull(document, "test connection test");

        assertEquals(testProduct.getName(), document.getName(), "testshould file test");
        assertEquals(testProduct.getDescription(), document.getDescription(), "testshould file test");
        assertEquals(testProduct.getPrice(), document.getPrice(), "test should file test");
        assertEquals(testProduct.getStatus().name(), document.getStatus(), "test connection file test");
        assertEquals(testProduct.getSeller().getName(), document.getSellerName(), "connectionshould file test");
        assertEquals(testProduct.getCategory().getName(), document.getCategoryName(), "Endpointshould file test");

        log.info("test test completed - test should: {}", response.hits().total().value());
        log.info("test: {}", document.getName());
    }

    @Test
    @Order(4)
    @DisplayName("connection Productshould connection should exists")
    void shouldSearchProductsByDifferentFields() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        productIndexingService.indexProduct(testProduct);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SearchResponse<ProductDocument> sellerResponse = productIndexingService.searchProducts("test connection");
        assertTrue(sellerResponse.hits().total().value() > 0, "connection testshould file test");
        SearchResponse<ProductDocument> categoryResponse = productIndexingService.searchProducts("file");
        assertTrue(categoryResponse.hits().total().value() > 0, "logging testshould file test");
        SearchResponse<ProductDocument> descriptionResponse = productIndexingService.searchProducts("test");
        assertTrue(descriptionResponse.hits().total().value() > 0, "file testshould file test");
        
        log.info("test test completed");
        log.info("- connection test: {}should", sellerResponse.hits().total().value());
        log.info("- processing test: {}should", categoryResponse.hits().total().value());
        log.info("- test: {}should", descriptionResponse.hits().total().value());
    }
}