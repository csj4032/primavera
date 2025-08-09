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
@DisplayName("ProductIndexingService 테스트")
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
                .name("전자제품")
                .level(1)
                .createdAt(LocalDateTime.now())
                .build();

        Seller seller = Seller.builder()
                .id(1L)
                .name("테스트 판매자")
                .email("seller@test.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("테스트 스마트폰")
                .description("최신 기술이 적용된 스마트폰입니다")
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
    @DisplayName("Products 인덱스를 생성할 수 있다")
    void shouldCreateProductsIndex() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        log.info("Products 인덱스 생성 테스트 완료");
    }

    @Test
    @Order(2) 
    @DisplayName("Product를 Elasticsearch에 인덱싱할 수 있다")
    void shouldIndexProduct() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        productIndexingService.indexProduct(testProduct);
        log.info("상품 인덱싱 테스트 완료 - Product ID: {}", testProduct.getId());
    }

    @Test
    @Order(3)
    @DisplayName("인덱싱된 Product를 검색할 수 있다") 
    void shouldSearchProducts() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        productIndexingService.indexProduct(testProduct);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SearchResponse<ProductDocument> response = productIndexingService.searchProducts("스마트폰");
        assertNotNull(response, "검색 응답이 null이 아니어야 한다");
        assertNotNull(response.hits(), "검색 결과가 있어야 한다");
        assertTrue(response.hits().total().value() > 0, "검색된 문서가 있어야 한다");
        Hit<ProductDocument> hit = response.hits().hits().get(0);
        ProductDocument document = hit.source();
        assertNotNull(document, "문서 소스가 있어야 한다");

        assertEquals(testProduct.getName(), document.getName(), "상품명이 일치해야 한다");
        assertEquals(testProduct.getDescription(), document.getDescription(), "상품 설명이 일치해야 한다");
        assertEquals(testProduct.getPrice(), document.getPrice(), "상품 가격이 일치해야 한다");
        assertEquals(testProduct.getStatus().name(), document.getStatus(), "상품 상태가 일치해야 한다");
        assertEquals(testProduct.getSeller().getName(), document.getSellerName(), "판매자명이 일치해야 한다");
        assertEquals(testProduct.getCategory().getName(), document.getCategoryName(), "카테고리명이 일치해야 한다");

        log.info("상품 검색 테스트 완료 - 검색된 문서 수: {}", response.hits().total().value());
        log.info("검색된 상품: {}", document.getName());
    }

    @Test
    @Order(4)
    @DisplayName("다양한 필드로 Product를 검색할 수 있다")
    void shouldSearchProductsByDifferentFields() throws IOException {
        productIndexingService.createProductsIndexIfNotExists();
        productIndexingService.indexProduct(testProduct);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SearchResponse<ProductDocument> sellerResponse = productIndexingService.searchProducts("테스트 판매자");
        assertTrue(sellerResponse.hits().total().value() > 0, "판매자명으로 검색이 가능해야 한다");
        SearchResponse<ProductDocument> categoryResponse = productIndexingService.searchProducts("전자제품");
        assertTrue(categoryResponse.hits().total().value() > 0, "카테고리명으로 검색이 가능해야 한다");
        SearchResponse<ProductDocument> descriptionResponse = productIndexingService.searchProducts("최신 기술");
        assertTrue(descriptionResponse.hits().total().value() > 0, "설명으로 검색이 가능해야 한다");
        
        log.info("다중 필드 검색 테스트 완료");
        log.info("- 판매자명 검색: {}개", sellerResponse.hits().total().value());
        log.info("- 카테고리명 검색: {}개", categoryResponse.hits().total().value());
        log.info("- 설명 검색: {}개", descriptionResponse.hits().total().value());
    }
}