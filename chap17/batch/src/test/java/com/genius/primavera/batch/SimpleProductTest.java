package com.genius.primavera.batch;

import com.genius.primavera.batch.repository.ProductRepository;
import com.genius.primavera.common.domain.Product;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Product Indexing Job 간단한 테스트")
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primavera")})
public class SimpleProductTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("데이터베이스에서 상품 데이터를 조회할 수 있다")
    void shouldFindProductsInDatabase() {
        List<Product> products = productRepository.findAll();
        assertFalse(products.isEmpty(), "상품 데이터가 존재해야 한다");
        log.info("Found {} products in database", products.size());
        products.forEach(product -> {
            assertNotNull(product.getId());
            assertNotNull(product.getName());
            assertNotNull(product.getSeller());
            assertNotNull(product.getCategory());
            log.info("Product: {} - {} (₩{})", product.getId(), product.getName(), product.getPrice());
        });
    }
}