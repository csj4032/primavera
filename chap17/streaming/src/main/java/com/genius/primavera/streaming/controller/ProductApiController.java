package com.genius.primavera.streaming.controller;

import com.genius.primavera.common.dto.ProductDocument;
import com.genius.primavera.streaming.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductSearchService productSearchService;

    @GetMapping(value = "/products/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductDocument> streamProducts() {
        return productSearchService.searchAllProducts()
                .repeat()
                .delayElements(Duration.ofMillis(100))
                .take(1000)
                .doOnSubscribe(subscription -> log.info("New SSE subscription for product streaming"))
                .doOnCancel(() -> log.info("SSE product streaming subscription cancelled"))
                .doOnError(error -> log.error("Error in product streaming", error));
    }

    @GetMapping(value = "/products/backpressure-demo", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> backpressureDemo() {
        return Flux.range(1, 10000)
                .map(i -> "Data item " + i + " generated at " + Instant.now())
                .delayElements(Duration.ofMillis(1))
                .doOnSubscribe(subscription -> log.info("Starting backpressure demo"))
                .doOnComplete(() -> log.info("Backpressure demo completed"))
                .doOnError(error -> log.error("Error in backpressure demo", error));
    }

    @PostMapping("/products/index")
    public Mono<String> indexProduct(@RequestBody ProductDocument product) {
        return productSearchService.indexProduct(product)
                .then(Mono.just("Product indexed successfully: " + product.getProductId()))
                .doOnSuccess(result -> log.info("Product {} indexed via API", product.getProductId()))
                .doOnError(error -> log.error("Failed to index product via API", error));
    }

    @GetMapping("/products/search")
    public Flux<ProductDocument> searchProducts(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        return productSearchService.searchProducts(query, category, minPrice, maxPrice, size)
                .doOnNext(product -> log.debug("API search found product: {}", product.getProductId()))
                .doOnComplete(() -> log.info("API search completed"))
                .doOnError(error -> log.error("API search error", error));
    }

    @PostMapping("/products/bulk-index")
    public Mono<String> bulkIndexProducts(@RequestBody List<ProductDocument> products) {
        return productSearchService.bulkIndexProducts(Flux.fromIterable(products))
                .map(response -> "Bulk indexing completed with " + products.size() + " products")
                .doOnSuccess(result -> log.info("Bulk indexing completed via API: {} products", products.size()))
                .doOnError(error -> log.error("Bulk indexing failed via API", error));
    }

    @GetMapping("/health")
    public Mono<String> health() {
        return productSearchService.getIndexHealth()
                .map(health -> "Elasticsearch health: " + health.get("status"))
                .onErrorReturn("Elasticsearch health: DOWN");
    }
}