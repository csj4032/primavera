package com.genius.primavera.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Category Domain Object Tests")
public class CategoryTest {

    @Test
    @DisplayName("Should create category with builder pattern")
    void shouldCreateCategoryWithBuilder() {
        // Given
        var now = LocalDateTime.now();

        // When
        var category = Category.builder()
            .id(1L)
            .name("Electronics")
            .level(1)
            .createdAt(now)
            .build();

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getName()).isEqualTo("Electronics");
        assertThat(category.getLevel()).isEqualTo(1);
        assertThat(category.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should create category with no-args constructor")
    void shouldCreateCategoryWithNoArgsConstructor() {
        // When
        var category = new Category();

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isNull();
        assertThat(category.getLevel()).isNull();
        assertThat(category.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should create category with all-args constructor")
    void shouldCreateCategoryWithAllArgsConstructor() {
        // Given
        var now = LocalDateTime.now();

        // When
        var category = new Category(1L, "Electronics", 1, now);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getName()).isEqualTo("Electronics");
        assertThat(category.getLevel()).isEqualTo(1);
        assertThat(category.getCreatedAt()).isEqualTo(now);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Should handle various category levels")
    void shouldHandleVariousCategoryLevels(int level) {
        // Given & When
        var category = Category.builder()
            .name("Test Category")
            .level(level)
            .build();

        // Then
        assertThat(category.getLevel()).isEqualTo(level);
    }

    @Test
    @DisplayName("Should support setter methods")
    void shouldSupportSetterMethods() {
        // Given
        var category = new Category();
        var now = LocalDateTime.now();

        // When
        category.setId(2L);
        category.setName("Updated Category");
        category.setLevel(2);
        category.setCreatedAt(now);

        // Then
        assertThat(category.getId()).isEqualTo(2L);
        assertThat(category.getName()).isEqualTo("Updated Category");
        assertThat(category.getLevel()).isEqualTo(2);
        assertThat(category.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should validate business rules for category creation")
    void shouldValidateBusinessRulesForCategoryCreation() {
        // Test valid category
        var validCategory = Category.builder()
            .name("Valid Category")
            .level(1)
            .createdAt(LocalDateTime.now())
            .build();

        assertThat(validCategory).isNotNull();
        assertThat(validCategory.getName()).isNotBlank();
        assertThat(validCategory.getLevel()).isPositive();
        assertThat(validCategory.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle hierarchical category levels")
    void shouldHandleHierarchicalCategoryLevels() {
        // Given - Create parent category
        var parentCategory = Category.builder()
            .id(1L)
            .name("Electronics")
            .level(1)
            .createdAt(LocalDateTime.now())
            .build();

        // When - Create child category
        var childCategory = Category.builder()
            .id(2L)
            .name("Laptops")
            .level(2)
            .createdAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(parentCategory.getLevel()).isLessThan(childCategory.getLevel());
        assertThat(childCategory.getLevel()).isEqualTo(parentCategory.getLevel() + 1);
    }

    @Test
    @DisplayName("Should support equality comparison")
    void shouldSupportEqualityComparison() {
        // Given
        var now = LocalDateTime.now();

        var category1 = Category.builder()
            .id(1L)
            .name("Electronics")
            .level(1)
            .createdAt(now)
            .build();

        var category2 = Category.builder()
            .id(1L)
            .name("Electronics")
            .level(1)
            .createdAt(now)
            .build();

        var category3 = Category.builder()
            .id(2L)
            .name("Books")
            .level(1)
            .createdAt(now)
            .build();

        // Then
        assertThat(category1).isEqualTo(category2);
        assertThat(category1).isNotEqualTo(category3);
        assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // When
        var category = Category.builder()
            .name(null)
            .level(null)
            .createdAt(null)
            .build();

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getName()).isNull();
        assertThat(category.getLevel()).isNull();
        assertThat(category.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle special category names")
    void shouldHandleSpecialCategoryNames() {
        // Test various category names
        String[] categoryNames = {
            "Electronics & Computers",
            "Home & Garden",
            "Sports & Outdoors",
            "Health & Beauty",
            "Books, Movies & Music",
            "Toys & Games"
        };

        for (String name : categoryNames) {
            var category = Category.builder()
                .name(name)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build();

            assertThat(category.getName()).isEqualTo(name);
            assertThat(category.getName()).isNotBlank();
        }
    }

    @Test
    @DisplayName("Should support deep category hierarchy")
    void shouldSupportDeepCategoryHierarchy() {
        // Create a deep category hierarchy: Electronics > Computers > Laptops > Gaming Laptops
        var level1 = Category.builder()
            .id(1L)
            .name("Electronics")
            .level(1)
            .createdAt(LocalDateTime.now())
            .build();

        var level2 = Category.builder()
            .id(2L)
            .name("Computers")
            .level(2)
            .createdAt(LocalDateTime.now())
            .build();

        var level3 = Category.builder()
            .id(3L)
            .name("Laptops")
            .level(3)
            .createdAt(LocalDateTime.now())
            .build();

        var level4 = Category.builder()
            .id(4L)
            .name("Gaming Laptops")
            .level(4)
            .createdAt(LocalDateTime.now())
            .build();

        // Verify hierarchy levels
        assertThat(level1.getLevel()).isEqualTo(1);
        assertThat(level2.getLevel()).isEqualTo(2);
        assertThat(level3.getLevel()).isEqualTo(3);
        assertThat(level4.getLevel()).isEqualTo(4);

        // Verify progressive level increase
        assertThat(level2.getLevel()).isGreaterThan(level1.getLevel());
        assertThat(level3.getLevel()).isGreaterThan(level2.getLevel());
        assertThat(level4.getLevel()).isGreaterThan(level3.getLevel());
    }
}