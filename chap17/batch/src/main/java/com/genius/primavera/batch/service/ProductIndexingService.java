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
        var exists = elasticsearchClient.indices()
            .exists(ExistsRequest.of(e -> e.index(PRODUCTS_INDEX)))
            .value();

        if (!exists) {
            var properties = createIndexMappingProperties();
            var response = elasticsearchClient.indices()
                .create(c -> c.index(PRODUCTS_INDEX).mappings(m -> m.properties(properties)));

            log.info("Products translated_text_3 creation completed: acknowledged={}", response.acknowledged());
        } else {
            log.info("Products translated_text_3 translated_text_2 translated_text_5");
        }
    }
    
    private Map<String, Property> createIndexMappingProperties() {
        return Map.of(
            "name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))),
            "description", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))),
            "price", Property.of(p -> p.integer(i -> i)),
            "status", Property.of(p -> p.keyword(k -> k)),
            "seller_name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))),
            "seller_email", Property.of(p -> p.keyword(k -> k)),
            "category_name", Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("standard")))),
            "created_at", Property.of(p -> p.date(d -> d)),
            "updated_at", Property.of(p -> p.date(d -> d))
        );
    }

    public void indexProduct(Product product) throws IOException {
        var document = convertToDocument(product);
        var response = elasticsearchClient.index(i -> i
            .index(PRODUCTS_INDEX)
            .id(product.getId().toString())
            .document(document));

        log.info("translated_text_2 translated_text_3 completed - ID: {}, Result: {}", response.id(), response.result());
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
        var status = switch (product.getStatus()) {
            case ACTIVE -> "ACTIVE";
            case INACTIVE -> "INACTIVE";
            case SOLD_OUT -> "SOLD_OUT";
        };

        return ProductDocument.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(status)
                .sellerName(product.getSeller().getName())
                .sellerEmail(product.getSeller().getEmail())
                .categoryName(product.getCategory().getName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}