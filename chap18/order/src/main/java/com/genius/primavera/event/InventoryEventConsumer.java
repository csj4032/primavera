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
            log.info("translated_text_2 translated_text_3 translated_text_2: eventType={}, topic={}", eventType, topic);
            
            switch (eventType) {
                case "INVENTORY_RESERVED" -> {
                    InventoryReservedEvent reservedEvent = (InventoryReservedEvent) event;
                    handleInventoryReservedEvent(reservedEvent);
                }
                case "INVENTORY_INSUFFICIENT" -> {
                    InventoryInsufficientEvent insufficientEvent = (InventoryInsufficientEvent) event;
                    handleInventoryInsufficientEvent(insufficientEvent);
                }
                default -> log.warn("translated_text_1 translated_text_1 translated_text_2 translated_text_3 translated_text_2: {}", eventType);
            }
            
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("translated_text_2 translated_text_3 processing translated_text_1 error: eventType={}, error={}", eventType, e.getMessage(), e);
            ack.acknowledge();
        }
    }
    
    private void handleInventoryReservedEvent(InventoryReservedEvent event) {
        log.info("translated_text_2 translated_text_2 completed: orderId={}, items={}", 
                event.getOrderId(), event.getReservedItems().size());

        orderService.confirmInventory(event.getOrderId())
                .doOnSuccess(order -> log.info("translated_text_2 translated_text_2 translated_text_4 completed: orderId={}, status={}", 
                        order.getOrderId(), order.getStatus()))
                .doOnError(error -> log.error("translated_text_2 translated_text_2 translated_text_4 failure: orderId={}, error={}", 
                        event.getOrderId(), error.getMessage()))
                .subscribe();
    }
    
    private void handleInventoryInsufficientEvent(InventoryInsufficientEvent event) {
        log.warn("translated_text_2 translated_text_4 translated_text_2 translated_text_2: orderId={}, reason={}, insufficientItems={}", 
                event.getOrderId(), event.getReason(), event.getInsufficientItems().size());

        orderService.cancelOrder(event.getOrderId(), event.getReason())
                .doOnSuccess(order -> log.info("translated_text_2 translated_text_2 completed: orderId={}, status={}", 
                        order.getOrderId(), order.getStatus()))
                .doOnError(error -> log.error("translated_text_2 translated_text_2 failure: orderId={}, error={}", 
                        event.getOrderId(), error.getMessage()))
                .subscribe();
    }
}