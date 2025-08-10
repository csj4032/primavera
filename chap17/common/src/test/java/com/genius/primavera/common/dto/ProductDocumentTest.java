package com.genius.primavera.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductDocument DTO Tests")
public class ProductDocumentTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should create ProductDocument with builder pattern")
    void shouldCreateProductDocumentWithBuilder() {
        // Given
        var sellerInfo = ProductDocument.SellerInfo.builder()
            .id(1L)
            .name("Test Seller")
            .email("test@seller.com")
            .rating(4.5)
            .build();

        var categoryInfo = ProductDocument.CategoryInfo.builder()
            .id(1L)
            .name("Electronics")
            .fullPath("Electronics > Computers > Laptops")
            .level(3)
            .build();

        var now = Instant.now();

        // When
        var productDocument = ProductDocument.builder()
            .productId(1L)
            .name("Gaming Laptop")
            .description("High-performance gaming laptop")
            .price(150000)
            .status("ACTIVE")
            .seller(sellerInfo)
            .category(categoryInfo)
            .searchKeywords(List.of("gaming", "laptop", "computer"))
            .priceRange("HIGH")
            .combinedText("Gaming Laptop High-performance gaming laptop")
            .indexedAt(now)
            .lastModified(now)
            .build();

        // Then
        assertThat(productDocument).isNotNull();
        assertThat(productDocument.getProductId()).isEqualTo(1L);
        assertThat(productDocument.getName()).isEqualTo("Gaming Laptop");
        assertThat(productDocument.getDescription()).isEqualTo("High-performance gaming laptop");
        assertThat(productDocument.getPrice()).isEqualTo(150000);
        assertThat(productDocument.getStatus()).isEqualTo("ACTIVE");
        assertThat(productDocument.getSeller()).isEqualTo(sellerInfo);
        assertThat(productDocument.getCategory()).isEqualTo(categoryInfo);
        assertThat(productDocument.getSearchKeywords()).containsExactly("gaming", "laptop", "computer");
        assertThat(productDocument.getPriceRange()).isEqualTo("HIGH");
        assertThat(productDocument.getCombinedText()).isEqualTo("Gaming Laptop High-performance gaming laptop");
        assertThat(productDocument.getIndexedAt()).isEqualTo(now);
        assertThat(productDocument.getLastModified()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should create ProductDocument with no-args constructor")
    void shouldCreateProductDocumentWithNoArgsConstructor() {
        // When
        var productDocument = new ProductDocument();

        // Then
        assertThat(productDocument).isNotNull();
        assertThat(productDocument.getProductId()).isNull();
        assertThat(productDocument.getName()).isNull();
        assertThat(productDocument.getDescription()).isNull();
        assertThat(productDocument.getPrice()).isNull();
        assertThat(productDocument.getStatus()).isNull();
        assertThat(productDocument.getSeller()).isNull();
        assertThat(productDocument.getCategory()).isNull();
        assertThat(productDocument.getSearchKeywords()).isNull();
        assertThat(productDocument.getPriceRange()).isNull();
        assertThat(productDocument.getCombinedText()).isNull();
        assertThat(productDocument.getIndexedAt()).isNull();
        assertThat(productDocument.getLastModified()).isNull();
    }

    @Test
    @DisplayName("Should create SellerInfo with builder pattern")
    void shouldCreateSellerInfoWithBuilder() {
        // When
        var sellerInfo = ProductDocument.SellerInfo.builder()
            .id(1L)
            .name("Test Seller")
            .email("test@seller.com")
            .rating(4.5)
            .build();

        // Then
        assertThat(sellerInfo).isNotNull();
        assertThat(sellerInfo.getId()).isEqualTo(1L);
        assertThat(sellerInfo.getName()).isEqualTo("Test Seller");
        assertThat(sellerInfo.getEmail()).isEqualTo("test@seller.com");
        assertThat(sellerInfo.getRating()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("Should create CategoryInfo with builder pattern")
    void shouldCreateCategoryInfoWithBuilder() {
        // When
        var categoryInfo = ProductDocument.CategoryInfo.builder()
            .id(1L)
            .name("Electronics")
            .fullPath("Electronics > Computers > Laptops")
            .level(3)
            .build();

        // Then
        assertThat(categoryInfo).isNotNull();
        assertThat(categoryInfo.getId()).isEqualTo(1L);
        assertThat(categoryInfo.getName()).isEqualTo("Electronics");
        assertThat(categoryInfo.getFullPath()).isEqualTo("Electronics > Computers > Laptops");
        assertThat(categoryInfo.getLevel()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should serialize and deserialize to/from JSON")
    void shouldSerializeAndDeserializeToFromJson() throws Exception {
        // Given
        var sellerInfo = ProductDocument.SellerInfo.builder()
            .id(1L)
            .name("Test Seller")
            .email("test@seller.com")
            .rating(4.5)
            .build();

        var categoryInfo = ProductDocument.CategoryInfo.builder()
            .id(1L)
            .name("Electronics")
            .fullPath("Electronics > Computers > Laptops")
            .level(3)
            .build();

        var originalDocument = ProductDocument.builder()
            .productId(1L)
            .name("Gaming Laptop")
            .description("High-performance gaming laptop")
            .price(150000)
            .status("ACTIVE")
            .seller(sellerInfo)
            .category(categoryInfo)
            .searchKeywords(List.of("gaming", "laptop", "computer"))
            .priceRange("HIGH")
            .combinedText("Gaming Laptop High-performance gaming laptop")
            .indexedAt(Instant.now())
            .lastModified(Instant.now())
            .build();

        // When
        String json = objectMapper.writeValueAsString(originalDocument);
        var deserializedDocument = objectMapper.readValue(json, ProductDocument.class);

        // Then
        assertThat(deserializedDocument).isNotNull();
        assertThat(deserializedDocument.getProductId()).isEqualTo(originalDocument.getProductId());
        assertThat(deserializedDocument.getName()).isEqualTo(originalDocument.getName());
        assertThat(deserializedDocument.getDescription()).isEqualTo(originalDocument.getDescription());
        assertThat(deserializedDocument.getPrice()).isEqualTo(originalDocument.getPrice());
        assertThat(deserializedDocument.getStatus()).isEqualTo(originalDocument.getStatus());
        assertThat(deserializedDocument.getSeller().getName()).isEqualTo(originalDocument.getSeller().getName());
        assertThat(deserializedDocument.getCategory().getName()).isEqualTo(originalDocument.getCategory().getName());
        assertThat(deserializedDocument.getSearchKeywords()).isEqualTo(originalDocument.getSearchKeywords());
        assertThat(deserializedDocument.getPriceRange()).isEqualTo(originalDocument.getPriceRange());
        assertThat(deserializedDocument.getCombinedText()).isEqualTo(originalDocument.getCombinedText());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // When
        var productDocument = ProductDocument.builder()
            .productId(null)
            .name(null)
            .description(null)
            .price(null)
            .status(null)
            .seller(null)
            .category(null)
            .searchKeywords(null)
            .priceRange(null)
            .combinedText(null)
            .indexedAt(null)
            .lastModified(null)
            .build();

        // Then
        assertThat(productDocument).isNotNull();
        assertThat(productDocument.getProductId()).isNull();
        assertThat(productDocument.getName()).isNull();
        assertThat(productDocument.getDescription()).isNull();
        assertThat(productDocument.getPrice()).isNull();
        assertThat(productDocument.getStatus()).isNull();
        assertThat(productDocument.getSeller()).isNull();
        assertThat(productDocument.getCategory()).isNull();
        assertThat(productDocument.getSearchKeywords()).isNull();
        assertThat(productDocument.getPriceRange()).isNull();
        assertThat(productDocument.getCombinedText()).isNull();
        assertThat(productDocument.getIndexedAt()).isNull();
        assertThat(productDocument.getLastModified()).isNull();
    }

    @Test
    @DisplayName("Should support equality comparison")
    void shouldSupportEqualityComparison() {
        // Given
        var sellerInfo1 = ProductDocument.SellerInfo.builder()
            .id(1L)
            .name("Test Seller")
            .email("test@seller.com")
            .rating(4.5)
            .build();

        var sellerInfo2 = ProductDocument.SellerInfo.builder()
            .id(1L)
            .name("Test Seller")
            .email("test@seller.com")
            .rating(4.5)
            .build();

        var categoryInfo1 = ProductDocument.CategoryInfo.builder()
            .id(1L)
            .name("Electronics")
            .fullPath("Electronics > Computers > Laptops")
            .level(3)
            .build();

        var categoryInfo2 = ProductDocument.CategoryInfo.builder()
            .id(1L)
            .name("Electronics")
            .fullPath("Electronics > Computers > Laptops")
            .level(3)
            .build();

        var now = Instant.now();

        var document1 = ProductDocument.builder()
            .productId(1L)
            .name("Gaming Laptop")
            .description("High-performance gaming laptop")
            .price(150000)
            .status("ACTIVE")
            .seller(sellerInfo1)
            .category(categoryInfo1)
            .searchKeywords(List.of("gaming", "laptop"))
            .priceRange("HIGH")
            .combinedText("Gaming Laptop High-performance gaming laptop")
            .indexedAt(now)
            .lastModified(now)
            .build();

        var document2 = ProductDocument.builder()
            .productId(1L)
            .name("Gaming Laptop")
            .description("High-performance gaming laptop")
            .price(150000)
            .status("ACTIVE")
            .seller(sellerInfo2)
            .category(categoryInfo2)
            .searchKeywords(List.of("gaming", "laptop"))
            .priceRange("HIGH")
            .combinedText("Gaming Laptop High-performance gaming laptop")
            .indexedAt(now)
            .lastModified(now)
            .build();

        var document3 = ProductDocument.builder()
            .productId(2L)
            .name("Different Product")
            .build();

        // Then
        assertThat(document1).isEqualTo(document2);
        assertThat(document1).isNotEqualTo(document3);
        assertThat(document1.hashCode()).isEqualTo(document2.hashCode());

        assertThat(sellerInfo1).isEqualTo(sellerInfo2);
        assertThat(categoryInfo1).isEqualTo(categoryInfo2);
    }

    @Test
    @DisplayName("Should have proper toString implementation")
    void shouldHaveProperToStringImplementation() {
        // Given
        var sellerInfo = ProductDocument.SellerInfo.builder()
            .id(1L)
            .name("Test Seller")
            .email("test@seller.com")
            .rating(4.5)
            .build();

        var categoryInfo = ProductDocument.CategoryInfo.builder()
            .id(1L)
            .name("Electronics")
            .fullPath("Electronics > Computers > Laptops")
            .level(3)
            .build();

        var productDocument = ProductDocument.builder()
            .productId(1L)
            .name("Gaming Laptop")
            .description("High-performance gaming laptop")
            .price(150000)
            .status("ACTIVE")
            .seller(sellerInfo)
            .category(categoryInfo)
            .searchKeywords(List.of("gaming", "laptop"))
            .priceRange("HIGH")
            .combinedText("Gaming Laptop High-performance gaming laptop")
            .indexedAt(Instant.now())
            .lastModified(Instant.now())
            .build();

        // When
        String productToString = productDocument.toString();
        String sellerToString = sellerInfo.toString();
        String categoryToString = categoryInfo.toString();

        // Then
        assertThat(productToString).isNotNull().isNotBlank();
        assertThat(productToString).contains("Gaming Laptop");
        assertThat(productToString).contains("150000");
        assertThat(productToString).contains("ACTIVE");

        assertThat(sellerToString).isNotNull().isNotBlank();
        assertThat(sellerToString).contains("Test Seller");
        assertThat(sellerToString).contains("test@seller.com");

        assertThat(categoryToString).isNotNull().isNotBlank();
        assertThat(categoryToString).contains("Electronics");
        assertThat(categoryToString).contains("3");
    }

    @Test
    @DisplayName("Should handle various price ranges")
    void shouldHandleVariousPriceRanges() {
        String[] priceRanges = {"LOW", "MEDIUM", "HIGH", "PREMIUM"};

        for (String priceRange : priceRanges) {
            var productDocument = ProductDocument.builder()
                .productId(1L)
                .name("Test Product")
                .priceRange(priceRange)
                .build();

            assertThat(productDocument.getPriceRange()).isEqualTo(priceRange);
        }
    }

    @Test
    @DisplayName("Should handle multiple search keywords")
    void shouldHandleMultipleSearchKeywords() {
        // Given
        var keywords = List.of(
            "laptop", "gaming", "computer", "electronics", 
            "high-performance", "portable", "technology"
        );

        // When
        var productDocument = ProductDocument.builder()
            .productId(1L)
            .name("Gaming Laptop")
            .searchKeywords(keywords)
            .build();

        // Then
        assertThat(productDocument.getSearchKeywords()).hasSize(7);
        assertThat(productDocument.getSearchKeywords()).containsExactlyElementsOf(keywords);
    }
}