package com.genius.primavera.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product Domain Object Tests")
public class ProductTest {

    @Test
    @DisplayName("Should create product with builder pattern")
    void shouldCreateProductWithBuilder() {
        // Given
        var category = createSampleCategory();
        var seller = createSampleSeller();
        var now = LocalDateTime.now();

        // When
        var product = Product.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .price(100000)
            .status(ProductStatus.ACTIVE)
            .category(category)
            .seller(seller)
            .createdAt(now)
            .updatedAt(now)
            .build();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getDescription()).isEqualTo("Test Description");
        assertThat(product.getPrice()).isEqualTo(100000);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getSeller()).isEqualTo(seller);
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should create product with no-args constructor")
    void shouldCreateProductWithNoArgsConstructor() {
        // When
        var product = new Product();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isNull();
        assertThat(product.getName()).isNull();
        assertThat(product.getDescription()).isNull();
        assertThat(product.getPrice()).isNull();
        assertThat(product.getStatus()).isNull();
        assertThat(product.getCategory()).isNull();
        assertThat(product.getSeller()).isNull();
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should create product with all-args constructor")
    void shouldCreateProductWithAllArgsConstructor() {
        // Given
        var category = createSampleCategory();
        var seller = createSampleSeller();
        var now = LocalDateTime.now();

        // When
        var product = new Product(1L, "Test Product", "Test Description", 
                                100000, ProductStatus.ACTIVE, seller, category, now, now);

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getDescription()).isEqualTo("Test Description");
        assertThat(product.getPrice()).isEqualTo(100000);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getSeller()).isEqualTo(seller);
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
    }

    @ParameterizedTest
    @EnumSource(ProductStatus.class)
    @DisplayName("Should handle all product statuses")
    void shouldHandleAllProductStatuses(ProductStatus status) {
        // Given
        var product = Product.builder()
            .name("Test Product")
            .status(status)
            .build();

        // When & Then
        assertThat(product.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 100, 1000, 999999, Integer.MAX_VALUE})
    @DisplayName("Should handle various price values")
    void shouldHandleVariousPriceValues(int price) {
        // Given & When
        var product = Product.builder()
            .name("Test Product")
            .price(price)
            .build();

        // Then
        assertThat(product.getPrice()).isEqualTo(price);
    }

    @Test
    @DisplayName("Should support setter methods")
    void shouldSupportSetterMethods() {
        // Given
        var product = new Product();
        var category = createSampleCategory();
        var seller = createSampleSeller();
        var now = LocalDateTime.now();

        // When
        product.setId(2L);
        product.setName("Updated Product");
        product.setDescription("Updated Description");
        product.setPrice(200000);
        product.setStatus(ProductStatus.INACTIVE);
        product.setCategory(category);
        product.setSeller(seller);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        // Then
        assertThat(product.getId()).isEqualTo(2L);
        assertThat(product.getName()).isEqualTo("Updated Product");
        assertThat(product.getDescription()).isEqualTo("Updated Description");
        assertThat(product.getPrice()).isEqualTo(200000);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getSeller()).isEqualTo(seller);
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should validate business rules for product creation")
    void shouldValidateBusinessRulesForProductCreation() {
        // Test valid product
        var validProduct = Product.builder()
            .name("Valid Product")
            .description("Valid Description")
            .price(100000)
            .status(ProductStatus.ACTIVE)
            .category(createSampleCategory())
            .seller(createSampleSeller())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        assertThat(validProduct).isNotNull();
        assertThat(validProduct.getName()).isNotBlank();
        assertThat(validProduct.getPrice()).isPositive();
        assertThat(validProduct.getStatus()).isNotNull();
        assertThat(validProduct.getCategory()).isNotNull();
        assertThat(validProduct.getSeller()).isNotNull();
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // When
        var product = Product.builder()
            .name(null)
            .description(null)
            .price(null)
            .status(null)
            .category(null)
            .seller(null)
            .createdAt(null)
            .updatedAt(null)
            .build();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getName()).isNull();
        assertThat(product.getDescription()).isNull();
        assertThat(product.getPrice()).isNull();
        assertThat(product.getStatus()).isNull();
        assertThat(product.getCategory()).isNull();
        assertThat(product.getSeller()).isNull();
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should support equality comparison")
    void shouldSupportEqualityComparison() {
        // Given
        var now = LocalDateTime.now();
        var category = createSampleCategory();
        var seller = createSampleSeller();

        var product1 = Product.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .price(100000)
            .status(ProductStatus.ACTIVE)
            .category(category)
            .seller(seller)
            .createdAt(now)
            .updatedAt(now)
            .build();

        var product2 = Product.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .price(100000)
            .status(ProductStatus.ACTIVE)
            .category(category)
            .seller(seller)
            .createdAt(now)
            .updatedAt(now)
            .build();

        var product3 = Product.builder()
            .id(2L)
            .name("Different Product")
            .build();

        // Then
        assertThat(product1).isEqualTo(product2);
        assertThat(product1).isNotEqualTo(product3);
        assertThat(product1.hashCode()).isEqualTo(product2.hashCode());
    }

    private Category createSampleCategory() {
        return Category.builder()
            .id(1L)
            .name("Test Category")
            .level(1)
            .createdAt(LocalDateTime.now())
            .build();
    }

    private Seller createSampleSeller() {
        return Seller.builder()
            .id(1L)
            .name("Test Seller")
            .email("test@seller.com")
            .rating(4.5)
            .createdAt(LocalDateTime.now())
            .build();
    }
}