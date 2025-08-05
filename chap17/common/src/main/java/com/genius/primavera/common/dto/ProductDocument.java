package com.genius.primavera.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {
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
    public static class SellerInfo {
        private Long id;
        private String name;
        private String email;
        private Double rating;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String fullPath;
        private Integer level;
    }
}