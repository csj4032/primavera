package com.genius.primavera.streaming.service;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.genius.primavera.common.dto.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticsearchAsyncClient elasticsearchAsyncClient;
    private static final String INDEX_NAME = "product_catalog_v1";

    public Flux<ProductDocument> searchProducts(String query, String category, Integer minPrice, Integer maxPrice, int size) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        if (query != null && !query.isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.multiMatch()
                    .fields("name", "description", "searchKeywords")
                    .query(query)
                    .build()._toQuery());
        }

        if (category != null && !category.isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.term()
                    .field("category.name.keyword")
                    .value(category)
                    .build()._toQuery());
        }

        if (minPrice != null || maxPrice != null) {
            var rangeBuilder = QueryBuilders.range().field("price");
            if (minPrice != null) rangeBuilder.gte(JsonData.of(minPrice));
            if (maxPrice != null) rangeBuilder.lte(JsonData.of(maxPrice));
            boolQueryBuilder.filter(rangeBuilder.build()._toQuery());
        }

        Query finalQuery = boolQueryBuilder.build()._toQuery();

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .query(finalQuery)
                .size(size)
                .trackTotalHits(t -> t.enabled(true))
        );

        CompletableFuture<SearchResponse<ProductDocument>> searchFuture = 
                elasticsearchAsyncClient.search(searchRequest, ProductDocument.class);

        return Mono.fromFuture(searchFuture)
                .flatMapMany(searchResponse -> {
                    List<ProductDocument> products = new ArrayList<>();
                    for (Hit<ProductDocument> hit : searchResponse.hits().hits()) {
                        if (hit.source() != null) {
                            products.add(hit.source());
                        }
                    }
                    log.info("Found {} products matching criteria", products.size());
                    return Flux.fromIterable(products);
                })
                .doOnError(error -> log.error("Search failed", error));
    }

    public Mono<Void> indexProduct(ProductDocument product) {
        IndexRequest<ProductDocument> indexRequest = IndexRequest.of(i -> i
                .index(INDEX_NAME)
                .id(String.valueOf(product.getProductId()))
                .document(product)
        );

        CompletableFuture<IndexResponse> indexFuture = 
                elasticsearchAsyncClient.index(indexRequest);

        return Mono.fromFuture(indexFuture)
                .doOnSuccess(response -> log.info("Product {} indexed with result: {}", 
                        product.getProductId(), response.result()))
                .doOnError(error -> log.error("Failed to index product {}", product.getProductId(), error))
                .then();
    }

    public Mono<Void> deleteProduct(Long productId) {
        DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                .index(INDEX_NAME)
                .id(String.valueOf(productId))
        );

        CompletableFuture<DeleteResponse> deleteFuture = 
                elasticsearchAsyncClient.delete(deleteRequest);

        return Mono.fromFuture(deleteFuture)
                .doOnSuccess(response -> log.info("Product {} deleted with result: {}", 
                        productId, response.result()))
                .doOnError(error -> log.error("Failed to delete product {}", productId, error))
                .then();
    }

    public Mono<Map<String, Object>> getIndexHealth() {
        CompletableFuture<HealthResponse> healthFuture =
                elasticsearchAsyncClient.cluster().health();

        return Mono.fromFuture(healthFuture)
                .map(healthResponse -> {
                    Map<String, Object> healthMap = new HashMap<>();
                    healthMap.put("status", healthResponse.status().toString());
                    healthMap.put("numberOfNodes", healthResponse.numberOfNodes());
                    healthMap.put("numberOfDataNodes", healthResponse.numberOfDataNodes());
                    healthMap.put("activePrimaryShards", healthResponse.activePrimaryShards());
                    healthMap.put("activeShards", healthResponse.activeShards());
                    healthMap.put("relocatingShards", healthResponse.relocatingShards());
                    healthMap.put("initializingShards", healthResponse.initializingShards());
                    healthMap.put("unassignedShards", healthResponse.unassignedShards());
                    return healthMap;
                })
                .doOnSuccess(health -> log.debug("Elasticsearch health: {}", health))
                .doOnError(error -> log.error("Failed to get Elasticsearch health", error));
    }

    public Mono<Void> bulkIndex(List<ProductDocument> products) {
        BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();

        for (ProductDocument product : products) {
            bulkRequestBuilder.operations(op -> op
                    .index(idx -> idx
                            .index(INDEX_NAME)
                            .id(String.valueOf(product.getProductId()))
                            .document(product)
                    )
            );
        }

        CompletableFuture<BulkResponse> bulkFuture = 
                elasticsearchAsyncClient.bulk(bulkRequestBuilder.build());

        return Mono.fromFuture(bulkFuture)
                .doOnSuccess(response -> {
                    if (response.errors()) {
                        log.error("Bulk indexing had errors");
                        response.items().forEach(item -> {
                            if (item.error() != null) {
                                log.error("Error indexing document {}: {}", 
                                        item.id(), item.error().reason());
                            }
                        });
                    } else {
                        log.info("Successfully bulk indexed {} documents", products.size());
                    }
                })
                .doOnError(error -> log.error("Bulk indexing failed", error))
                .then();
    }
}