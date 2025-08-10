package com.genius.primavera.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.order-events:order-events}")
    private String orderEventsTopic;
    
    @Value("${kafka.topics.inventory-events:inventory-events}")
    private String inventoryEventsTopic;
    
    public Mono<Void> publishOrderCreatedEvent(OrderCreatedEvent event) {
        return Mono.fromCallable(() -> {
            event.setEventType("ORDER_CREATED");
            event.setVersion("1.0");
            event.setTimestamp(Instant.now());
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(orderEventsTopic, event.getOrderId(), event);
            
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("translated_text_2 creation translated_text_3 translated_text_2 failure: orderId={}, error={}", 
                            event.getOrderId(), throwable.getMessage());
                } else {
                    log.info("translated_text_2 creation translated_text_3 translated_text_2 success: orderId={}, partition={}, offset={}", 
                            event.getOrderId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
            
            return null;
        }).then();
    }
    
    public Mono<Void> publishOrderCancelledEvent(String orderId, String reason) {
        return Mono.fromCallable(() -> {
            OrderCancelledEvent event = OrderCancelledEvent.builder()
                    .orderId(orderId)
                    .reason(reason)
                    .timestamp(Instant.now())
                    .eventType("ORDER_CANCELLED")
                    .version("1.0")
                    .build();
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(orderEventsTopic, orderId, event);
            
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("translated_text_2 translated_text_2 translated_text_3 translated_text_2 failure: orderId={}, error={}", 
                            orderId, throwable.getMessage());
                } else {
                    log.info("translated_text_2 translated_text_2 translated_text_3 translated_text_2 success: orderId={}, reason={}", 
                            orderId, reason);
                }
            });
            
            return null;
        }).then();
    }
}