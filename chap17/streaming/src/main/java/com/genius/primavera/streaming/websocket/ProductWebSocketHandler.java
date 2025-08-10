package com.genius.primavera.streaming.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.common.dto.ProductDocument;
import com.genius.primavera.streaming.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductWebSocketHandler implements WebSocketHandler {

    private final ProductSearchService productSearchService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("New WebSocket connection established: {}", session.getId());

        Flux<WebSocketMessage> output = productSearchService.searchAllProducts()
                .repeat()
                .delayElements(Duration.ofSeconds(2))
                .take(100)
                .map(this::productToJson)
                .map(session::textMessage)
                .doOnNext(message -> log.debug("Sending WebSocket message to session: {}", session.getId()))
                .doOnComplete(() -> log.info("WebSocket stream completed for session: {}", session.getId()))
                .doOnError(error -> log.error("WebSocket error for session: {}", session.getId(), error));

        return session.send(output)
                .doOnTerminate(() -> log.info("WebSocket connection terminated: {}", session.getId()));
    }

    private String productToJson(ProductDocument product) {
        try {
            return objectMapper.writeValueAsString(product);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize product to JSON", e);
            return "{\"error\":\"Serialization failed\"}";
        }
    }
}