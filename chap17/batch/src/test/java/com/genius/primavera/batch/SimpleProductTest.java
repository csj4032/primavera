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
@DisplayName("Product Indexing Job translated_text_3 test")
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primavera")})
public class SimpleProductTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("translated_text_9 translated_text_2 translated_text_5 translated_text_8 translated_text_1 exists")
    void shouldFindProductsInDatabase() {
        List<Product> products = productRepository.findAll();
        assertFalse(products.isEmpty(), "translated_text_2 translated_text_5 translated_text_4 translated_text_2");
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