package com.genius.primavera.streaming.handler;

import com.genius.primavera.common.dto.ProductDocument;
import com.genius.primavera.streaming.service.ProductSearchService;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Debezium CDC 이벤트 핸들러 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableTestContainers({
        @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "mariadb"),
        @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")
})
public class DebeziumCdcEventHandlerIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ProductSearchService productSearchService;

    @Autowired
    private DebeziumCdcEventHandler debeziumCdcEventHandler;

    @BeforeEach
    void setUp() {
        when(productSearchService.indexProduct(any(ProductDocument.class))).thenReturn(Mono.empty());
        when(productSearchService.deleteProduct(anyLong())).thenReturn(Mono.empty());
    }

    @Test
    @Order(1)
    @DisplayName("CDC 핸들러가 정상적으로 시작되는지 확인")
    void debeziumCdcHandlerShouldStart() {
        assertThat(debeziumCdcEventHandler).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("상품 생성 시 CDC 이벤트가 처리되는지 확인")
    void shouldHandleProductCreateEvent() {
        String insertSql = "INSERT INTO PRODUCTS (name, description, price, category, created_at, updated_at) VALUES ('Test Product', 'Test Product Description', 200000.00, 'Electronics', NOW(), NOW())";
        jdbcTemplate.update(insertSql);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> verify(productSearchService, atLeastOnce()).indexProduct(any(ProductDocument.class)));
    }

    @Test
    @Order(3)
    @DisplayName("상품 수정 시 CDC 이벤트가 처리되는지 확인")
    void shouldHandleProductUpdateEvent() {
        String updateSql = "UPDATE PRODUCTS SET name = 'Updated Product', price = 60000 WHERE id = 4";
        jdbcTemplate.update(updateSql);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> verify(productSearchService, atLeast(2)).indexProduct(any(ProductDocument.class)));
    }

    @Test
    @Order(4)
    @DisplayName("상품 삭제 시 CDC 이벤트가 처리되는지 확인")
    void shouldHandleProductDeleteEvent() {
        String deleteSql = "DELETE FROM PRODUCTS WHERE id = 4";
        jdbcTemplate.update(deleteSql);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> verify(productSearchService, atLeastOnce()).deleteProduct(anyLong()));
    }

    @Test
    @Order(5)
    @DisplayName("CDC 이벤트 처리 시 올바른 ProductDocument 생성 확인")
    void shouldCreateCorrectProductDocument() {
        String insertSql = "INSERT INTO PRODUCTS (name, description, price, status) VALUES ('Document Test Product', 'Document Test Description', 75000, 'ACTIVE')";
        jdbcTemplate.update(insertSql);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(productSearchService, atLeastOnce()).indexProduct(argThat(product ->
                    "Document Test Product".equals(product.getName()) &&
                            "Document Test Description".equals(product.getDescription()) &&
                            product.getPrice().equals(75000) &&
                            "ACTIVE".equals(product.getStatus()) &&
                            "MEDIUM".equals(product.getPriceRange())
            ));
        });
    }
}