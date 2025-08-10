package com.genius.primavera.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@lombok.EqualsAndHashCode
public final class ProductDocument {
    private Long productId;
    private String name;
    private String description;
    private Integer price;
    private String status;
    private SellerInfo seller;
    private CategoryInfo category;
    private List<String> searchKeywords;
    private String priceRange;
    private String combinedText;
    private Instant indexedAt;
    private Instant lastModified;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @lombok.EqualsAndHashCode
    public static final class SellerInfo {
        private Long id;
        private String name;
        private String email;
        private Double rating;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @lombok.EqualsAndHashCode
    public static final class CategoryInfo {
        private Long id;
        private String name;
        private String fullPath;
        private Integer level;
    }
}