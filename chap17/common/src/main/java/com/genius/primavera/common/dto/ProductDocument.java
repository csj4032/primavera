package com.genius.primavera.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;

@Document(indexName = "product_test_fixed")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@lombok.EqualsAndHashCode
public final class ProductDocument {
    @Id
    private Long productId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Text)
    private String description;
    
    @Field(type = FieldType.Integer)
    private Integer price;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Object)
    private SellerInfo seller;
    
    @Field(type = FieldType.Object)
    private CategoryInfo category;
    
    @Field(type = FieldType.Keyword)
    private List<String> searchKeywords;
    
    @Field(type = FieldType.Keyword)
    private String priceRange;
    
    @Field(type = FieldType.Text)
    private String combinedText;
    
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Instant indexedAt;
    
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
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