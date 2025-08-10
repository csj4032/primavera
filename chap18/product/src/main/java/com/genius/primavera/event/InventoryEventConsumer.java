package com.genius.primavera.event;

import com.genius.primavera.ProductServiceImpl;
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
    
    private final ProductServiceImpl productService;
    
    @KafkaListener(
        topics = "order-events",
        groupId = "inventory-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        try {
            log.info("translated_text_2 creation translated_text_3 translated_text_2: orderId={}, items={}, topic={}, partition={}, offset={}", 
                    event.getOrderId(), event.getItems().size(), topic, partition, offset);

            productService.processInventoryReservation(event)
                    .doOnSuccess(result -> {
                        log.info("translated_text_2 processing completed: orderId={}, success={}", 
                                event.getOrderId(), result.isSuccess());
                        ack.acknowledge();
                    })
                    .doOnError(error -> {
                        log.error("translated_text_2 processing translated_text_1 error: orderId={}, error={}", 
                                event.getOrderId(), error.getMessage(), error);

                        ack.acknowledge();
                    })
                    .subscribe();
            
        } catch (Exception e) {
            log.error("translated_text_3 processing translated_text_1 exception translated_text_2: orderId={}, error={}", 
                    event.getOrderId(), e.getMessage(), e);
            ack.acknowledge();
        }
    }
}