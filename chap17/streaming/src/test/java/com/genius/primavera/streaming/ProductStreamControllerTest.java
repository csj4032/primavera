package com.genius.primavera.streaming;

import com.genius.primavera.common.dto.ProductDocument;
import com.genius.primavera.streaming.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("ProductStreamController 통합 테스트")
class ProductStreamControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductSearchService productSearchService;

    private ProductDocument testProduct;

    @BeforeEach
    void setUp() {
        testProduct = ProductDocument.builder()
                .productId(1L)
                .name("Test Product")
                .description("Test Description")
                .price(100000)
                .status("ACTIVE")
                .seller(ProductDocument.SellerInfo.builder()
                        .id(1L)
                        .name("Test Seller")
                        .email("test@seller.com")
                        .rating(4.5)
                        .build())
                .category(ProductDocument.CategoryInfo.builder()
                        .id(1L)
                        .name("Test Category")
                        .fullPath("Electronics > Test Category")
                        .level(2)
                        .build())
                .searchKeywords(List.of("test", "product"))
                .priceRange("MEDIUM")
                .indexedAt(Instant.now())
                .lastModified(Instant.now())
                .build();

        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Test
    @DisplayName("상품 검색 API가 정상적으로 작동한다")
    void shouldSearchProductsSuccessfully() {
        when(productSearchService.searchProducts(anyString(), anyString(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Flux.just(testProduct));

        webTestClient.get()
                .uri("/stream/search?query=test&size=10")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON)
                .expectBodyList(ProductDocument.class)
                .hasSize(1)
                .consumeWith(result -> {
                    List<ProductDocument> products = result.getResponseBody();
                    assert products != null;
                    assert products.get(0).getName().equals("Test Product");
                    log.info("검색된 상품: {}", products.get(0).getName());
                });
    }

    @Test
    @DisplayName("상품 색인 API가 정상적으로 작동한다")
    void shouldIndexProductSuccessfully() {
        when(productSearchService.indexProduct(any(ProductDocument.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/stream/products/index")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testProduct)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    Map<String, Object> response = result.getResponseBody();
                    assert response != null;
                    assert Boolean.TRUE.equals(response.get("success"));
                    assert response.get("productId").equals(1);
                    log.info("색인 응답: {}", response);
                });
    }

    @Test
    @DisplayName("상품 삭제 API가 정상적으로 작동한다")
    void shouldDeleteProductSuccessfully() {
        when(productSearchService.deleteProduct(anyLong()))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/stream/products/{productId}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    Map<String, Object> response = result.getResponseBody();
                    assert response != null;
                    assert Boolean.TRUE.equals(response.get("success"));
                    assert response.get("productId").equals(1);
                    log.info("삭제 응답: {}", response);
                });
    }

    @Test
    @DisplayName("헬스 체크 API가 정상적으로 작동한다")
    void shouldReturnHealthStatus() {
        Map<String, Object> healthInfo = Map.of(
                "status", "GREEN",
                "numberOfNodes", 1,
                "activePrimaryShards", 5
        );

        when(productSearchService.getIndexHealth())
                .thenReturn(Mono.just(healthInfo));

        webTestClient.get()
                .uri("/stream/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    Map<String, Object> response = result.getResponseBody();
                    assert response != null;
                    assert "UP".equals(response.get("status"));
                    assert response.get("elasticsearch") != null;
                    log.info("헬스 체크 응답: {}", response);
                });
    }

    @Test
    @DisplayName("SSE 이벤트 스트림이 정상적으로 작동한다")
    void shouldStreamEventsViaSSE() {
        webTestClient.get()
                .uri("/stream/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .returnResult(String.class)
                .getResponseBody()
                .take(Duration.ofSeconds(3))
                .doOnNext(event -> log.info("받은 SSE 이벤트: {}", event))
                .blockFirst(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("상품 스트림이 정상적으로 작동한다")
    void shouldStreamProducts() {
        webTestClient.get()
                .uri("/stream/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8");
    }

    @Test
    @DisplayName("가격 범위 검색이 정상적으로 작동한다")
    void shouldSearchByPriceRange() {
        when(productSearchService.searchProducts(isNull(), isNull(), eq(50000), eq(150000), eq(5)))
                .thenReturn(Flux.just(testProduct));

        webTestClient.get()
                .uri("/stream/search?minPrice=50000&maxPrice=150000&size=5")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductDocument.class)
                .hasSize(1)
                .consumeWith(result -> {
                    List<ProductDocument> products = result.getResponseBody();
                    assert products != null;
                    ProductDocument product = products.get(0);
                    assert product.getPrice() >= 50000 && product.getPrice() <= 150000;
                    log.info("가격 범위 검색 결과: {} (₩{})", product.getName(), product.getPrice());
                });
    }

    @Test
    @DisplayName("카테고리별 검색이 정상적으로 작동한다")
    void shouldSearchByCategory() {
        when(productSearchService.searchProducts(isNull(), eq("Test Category"), isNull(), isNull(), eq(10)))
                .thenReturn(Flux.just(testProduct));

        webTestClient.get()
                .uri("/stream/search?category=Test Category")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductDocument.class)
                .hasSize(1)
                .consumeWith(result -> {
                    List<ProductDocument> products = result.getResponseBody();
                    assert products != null;
                    ProductDocument product = products.get(0);
                    assert "Test Category".equals(product.getCategory().getName());
                    log.info("카테고리별 검색 결과: {} (카테고리: {})", 
                            product.getName(), product.getCategory().getName());
                });
    }

    @Test
    @DisplayName("서비스 에러 시 적절히 처리한다")
    void shouldHandleServiceErrors() {
        when(productSearchService.indexProduct(any(ProductDocument.class)))
                .thenReturn(Mono.error(new RuntimeException("Elasticsearch connection failed")));

        webTestClient.post()
                .uri("/stream/products/index")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testProduct)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("헬스 체크 실패 시 DOWN 상태를 반환한다")
    void shouldReturnDownStatusOnHealthCheckFailure() {
        when(productSearchService.getIndexHealth())
                .thenReturn(Mono.error(new RuntimeException("Connection failed")));

        webTestClient.get()
                .uri("/stream/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    Map<String, Object> response = result.getResponseBody();
                    assert response != null;
                    assert "DOWN".equals(response.get("status"));
                    assert response.get("error") != null;
                    log.info("헬스 체크 실패 응답: {}", response);
                });
    }
}