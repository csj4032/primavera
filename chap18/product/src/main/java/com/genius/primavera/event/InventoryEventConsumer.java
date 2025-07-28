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
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        
        try {
            log.info("주문 생성 이벤트 수신: orderId={}, items={}, topic={}, partition={}, offset={}", 
                    event.getOrderId(), event.getItems().size(), topic, partition, offset);
            
            // 재고 확인 및 차감 처리
            productService.processInventoryReservation(event)
                    .doOnSuccess(result -> {
                        log.info("재고 처리 완료: orderId={}, success={}", 
                                event.getOrderId(), result.isSuccess());
                        ack.acknowledge(); // 수동 커밋
                    })
                    .doOnError(error -> {
                        log.error("재고 처리 중 오류: orderId={}, error={}", 
                                event.getOrderId(), error.getMessage(), error);
                        // 에러 발생 시에도 ACK (DLQ 또는 별도 처리 필요)
                        ack.acknowledge();
                    })
                    .subscribe();
            
        } catch (Exception e) {
            log.error("이벤트 처리 중 예외 발생: orderId={}, error={}", 
                    event.getOrderId(), e.getMessage(), e);
            ack.acknowledge();
        }
    }
}