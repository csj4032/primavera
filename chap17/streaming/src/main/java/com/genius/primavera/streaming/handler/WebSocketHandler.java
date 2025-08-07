package com.genius.primavera.streaming.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.common.dto.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler implements org.springframework.web.reactive.socket.WebSocketHandler {

    private final Sinks.Many<ProductDocument> productSink;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();
        log.info("WebSocket session started: {}", sessionId);

        Flux<WebSocketMessage> output = productSink.asFlux()
                .map(this::convertToWebSocketMessage)
                .map(session::textMessage)
                .doOnNext(message -> log.debug("Sending message to session {}: {}", sessionId, message.getPayloadAsText()))
                .onErrorResume(error -> {
                    log.error("Error in WebSocket stream for session {}", sessionId, error);
                    return Flux.empty();
                });

        Mono<Void> input = session.receive()
                .doOnNext(message -> log.debug("Received message from session {}: {}", sessionId, message.getPayloadAsText()))
                .doOnError(error -> log.error("Error receiving message from session {}", sessionId, error))
                .then();

        return Mono.zip(
                session.send(output),
                input
        )
        .then()
        .doFinally(signalType -> log.info("WebSocket session ended: {} with signal: {}", sessionId, signalType))
        .timeout(Duration.ofMinutes(30))
        .onErrorResume(error -> {
            log.error("WebSocket session error for {}", sessionId, error);
            return Mono.empty();
        });
    }

    private String convertToWebSocketMessage(ProductDocument product) {
        try {
            return objectMapper.writeValueAsString(product);
        } catch (Exception e) {
            log.error("Failed to serialize product to JSON", e);
            return "{}";
        }
    }
}