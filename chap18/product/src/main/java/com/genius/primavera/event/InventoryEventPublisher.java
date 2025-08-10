package com.genius.primavera.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.inventory-events:inventory-events}")
    private String inventoryEventsTopic;
    
    public Mono<Void> publishInventoryReservedEvent(String orderId, List<OrderItemEvent> items) {
        return Mono.fromCallable(() -> {
            InventoryReservedEvent event = InventoryReservedEvent.builder()
                    .orderId(orderId)
                    .reservedItems(items)
                    .timestamp(Instant.now())
                    .eventType("INVENTORY_RESERVED")
                    .version("1.0")
                    .build();
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(inventoryEventsTopic, orderId, event);
            
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("test connection test failure: orderId={}, error={}", 
                            orderId, throwable.getMessage());
                } else {
                    log.info("test connection test success: orderId={}", orderId);
                }
            });
            
            return null;
        }).then();
    }
    
    public Mono<Void> publishInventoryInsufficientEvent(String orderId, List<InsufficientItemEvent> insufficientItems, String reason) {
        return Mono.fromCallable(() -> {
            InventoryInsufficientEvent event = InventoryInsufficientEvent.builder()
                    .orderId(orderId)
                    .insufficientItems(insufficientItems)
                    .reason(reason)
                    .timestamp(Instant.now())
                    .eventType("INVENTORY_INSUFFICIENT")
                    .version("1.0")
                    .build();
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(inventoryEventsTopic, orderId, event);
            
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("test connection test failure: orderId={}, error={}", 
                            orderId, throwable.getMessage());
                } else {
                    log.info("test connection test success: orderId={}, reason={}", orderId, reason);
                }
            });
            
            return null;
        }).then();
    }
}