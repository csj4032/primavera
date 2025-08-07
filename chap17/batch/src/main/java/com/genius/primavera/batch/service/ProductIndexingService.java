package com.genius.primavera.batch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.genius.primavera.batch.elasticsearch.ProductDocument;
import com.genius.primavera.common.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexingService {

    private final ElasticsearchClient elasticsearchClient;
    
    private static final String PRODUCTS_INDEX = "products";

    public void createProductsIndexIfNotExists() throws IOException {
        boolean exists = elasticsearchClient.indices().exists(
            ExistsRequest.of(e -> e.index(PRODUCTS_INDEX))
        ).value();

        if (!exists) {
            Map<String, Property> properties = new HashMap<>();
            properties.put("name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
            properties.put("description", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
            properties.put("price", Property.of(p -> p.integer(i -> i)));
            properties.put("status", Property.of(p -> p.keyword(k -> k)));
            properties.put("seller_name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));
            properties.put("seller_email", Property.of(p -> p.keyword(k -> k)));
            properties.put("category_name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))));;
            properties.put("created_at", Property.of(p -> p.date(d -> d)));
            properties.put("updated_at", Property.of(p -> p.date(d -> d)));

            CreateIndexResponse response = elasticsearchClient.indices().create(c -> c
                    .index(PRODUCTS_INDEX)
                    .mappings(m -> m.properties(properties))
            );

            log.info("Products 인덱스 생성 완료: acknowledged={}", response.acknowledged());
        } else {
            log.info("Products 인덱스가 이미 존재합니다");
        }
    }

    public void indexProduct(Product product) throws IOException {
        ProductDocument document = convertToDocument(product);
        
        IndexResponse response = elasticsearchClient.index(i -> i
                .index(PRODUCTS_INDEX)
                .id(product.getId().toString())
                .document(document)
        );

        log.info("상품 인덱싱 완료 - ID: {}, Result: {}", response.id(), response.result());
    }

    public SearchResponse<ProductDocument> searchProducts(String query) throws IOException {
        return elasticsearchClient.search(s -> s
                .index(PRODUCTS_INDEX)
                .query(q -> q
                        .multiMatch(m -> m
                                .fields("name", "description", "seller_name", "category_name")
                                .query(query)
                        )
                ), ProductDocument.class
        );
    }

    private ProductDocument convertToDocument(Product product) {
        return ProductDocument.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus().name())
                .sellerName(product.getSeller().getName())
                .sellerEmail(product.getSeller().getEmail())
                .categoryName(product.getCategory().getName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}