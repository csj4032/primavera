package com.genius.primavera.event;

import com.genius.primavera.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {
    
    private final OrderServiceImpl orderService;
    
    @KafkaListener(
        topics = "inventory-events",
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryEvents(
            @Payload Object event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header("eventType") String eventType,
            Acknowledgment ack) {
        
        try {
            log.info("test connection test: eventType={}, topic={}", eventType, topic);
            
            switch (eventType) {
                case "INVENTORY_RESERVED" -> {
                    InventoryReservedEvent reservedEvent = (InventoryReservedEvent) event;
                    handleInventoryReservedEvent(reservedEvent);
                }
                case "INVENTORY_INSUFFICIENT" -> {
                    InventoryInsufficientEvent insufficientEvent = (InventoryInsufficientEvent) event;
                    handleInventoryInsufficientEvent(insufficientEvent);
                }
                default -> log.warn("needs to be added test connection test: {}", eventType);
            }
            
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("test connection processing should error: eventType={}, error={}", eventType, e.getMessage(), e);
            ack.acknowledge();
        }
    }
    
    private void handleInventoryReservedEvent(InventoryReservedEvent event) {
        log.info("test completed: orderId={}, items={}", 
                event.getOrderId(), event.getReservedItems().size());

        orderService.confirmInventory(event.getOrderId())
                .doOnSuccess(order -> log.info("test file completed: orderId={}, status={}", 
                        order.getOrderId(), order.getStatus()))
                .doOnError(error -> log.error("test file failure: orderId={}, error={}", 
                        event.getOrderId(), error.getMessage()))
                .subscribe();
    }
    
    private void handleInventoryInsufficientEvent(InventoryInsufficientEvent event) {
        log.warn("test file test: orderId={}, reason={}, insufficientItems={}", 
                event.getOrderId(), event.getReason(), event.getInsufficientItems().size());

        orderService.cancelOrder(event.getOrderId(), event.getReason())
                .doOnSuccess(order -> log.info("test completed: orderId={}, status={}", 
                        order.getOrderId(), order.getStatus()))
                .doOnError(error -> log.error("test failure: orderId={}, error={}", 
                        event.getOrderId(), error.getMessage()))
                .subscribe();
    }
}