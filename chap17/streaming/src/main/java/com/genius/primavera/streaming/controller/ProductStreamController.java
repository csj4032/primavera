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
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor
public class ProductStreamController {

    private final ProductSearchService productSearchService;
    private final Sinks.Many<ProductDocument> productEventSink = Sinks.many().multicast().onBackpressureBuffer();

    @GetMapping(value = "/products", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ProductDocument>> streamProducts() {
        return productEventSink.asFlux()
                .map(product -> ServerSentEvent.<ProductDocument>builder()
                        .id(UUID.randomUUID().toString())
                        .event("product-update")
                        .data(product)
                        .build())
                .doOnSubscribe(subscription -> log.info("New SSE subscription for products"))
                .doOnCancel(() -> log.info("SSE subscription cancelled"))
                .doOnError(error -> log.error("Error in SSE stream", error));
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(5))
                .map(sequence -> {
                    Map<String, Object> event = new HashMap<>();
                    event.put("sequence", sequence);
                    event.put("timestamp", LocalDateTime.now());
                    event.put("type", "heartbeat");
                    event.put("message", "System is running");
                    
                    return ServerSentEvent.<Map<String, Object>>builder()
                            .id(String.valueOf(sequence))
                            .event("system-heartbeat")
                            .data(event)
                            .retry(Duration.ofSeconds(10))
                            .build();
                })
                .doOnSubscribe(subscription -> log.info("New SSE subscription for events"))
                .doOnCancel(() -> log.info("SSE events subscription cancelled"));
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ProductDocument> searchProducts(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        log.info("Search request - query: {}, category: {}, price: {}-{}, size: {}", 
                query, category, minPrice, maxPrice, size);
        
        return productSearchService.searchProducts(query, category, minPrice, maxPrice, size)
                .doOnNext(product -> log.debug("Found product: {}", product.getProductId()))
                .doOnComplete(() -> log.info("Search completed"))
                .doOnError(error -> log.error("Search error", error));
    }

    @PostMapping("/products/index")
    public Mono<Map<String, Object>> indexProduct(@RequestBody ProductDocument product) {
        return productSearchService.indexProduct(product)
                .then(Mono.fromRunnable(() -> productEventSink.tryEmitNext(product)))
                .then(Mono.fromCallable(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("productId", product.getProductId());
                    response.put("message", "Product indexed successfully");
                    response.put("timestamp", LocalDateTime.now());
                    return response;
                }))
                .doOnSuccess(result -> log.info("Product {} indexed successfully", product.getProductId()))
                .doOnError(error -> log.error("Failed to index product", error));
    }

    @DeleteMapping("/products/{productId}")
    public Mono<Map<String, Object>> deleteProduct(@PathVariable Long productId) {
        return productSearchService.deleteProduct(productId)
                .then(Mono.fromCallable(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("productId", productId);
                    response.put("message", "Product deleted from index");
                    response.put("timestamp", LocalDateTime.now());
                    return response;
                }))
                .doOnSuccess(result -> log.info("Product {} deleted from index", productId))
                .doOnError(error -> log.error("Failed to delete product {}", productId, error));
    }

    @GetMapping("/health")
    public Mono<Map<String, Object>> healthCheck() {
        return productSearchService.getIndexHealth()
                .map(health -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "UP");
                    response.put("elasticsearch", health);
                    response.put("timestamp", LocalDateTime.now());
                    return response;
                })
                .doOnSuccess(health -> log.debug("Health check completed"))
                .onErrorReturn(new HashMap<String, Object>() {{
                    put("status", "DOWN");
                    put("error", "Failed to connect to Elasticsearch");
                    put("timestamp", LocalDateTime.now());
                }});
    }
}