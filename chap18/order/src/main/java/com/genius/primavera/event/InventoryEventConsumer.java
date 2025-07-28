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
            log.info("재고 이벤트 수신: eventType={}, topic={}", eventType, topic);
            
            switch (eventType) {
                case "INVENTORY_RESERVED" -> {
                    InventoryReservedEvent reservedEvent = (InventoryReservedEvent) event;
                    handleInventoryReservedEvent(reservedEvent);
                }
                case "INVENTORY_INSUFFICIENT" -> {
                    InventoryInsufficientEvent insufficientEvent = (InventoryInsufficientEvent) event;
                    handleInventoryInsufficientEvent(insufficientEvent);
                }
                default -> log.warn("알 수 없는 이벤트 타입: {}", eventType);
            }
            
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("재고 이벤트 처리 중 오류: eventType={}, error={}", eventType, e.getMessage(), e);
            ack.acknowledge();
        }
    }
    
    private void handleInventoryReservedEvent(InventoryReservedEvent event) {
        log.info("재고 예약 완료: orderId={}, items={}", 
                event.getOrderId(), event.getReservedItems().size());
        
        // 주문 상태를 재고 확인됨으로 변경
        orderService.confirmInventory(event.getOrderId())
                .doOnSuccess(order -> log.info("주문 상태 업데이트 완료: orderId={}, status={}", 
                        order.getOrderId(), order.getStatus()))
                .doOnError(error -> log.error("주문 상태 업데이트 실패: orderId={}, error={}", 
                        event.getOrderId(), error.getMessage()))
                .subscribe();
    }
    
    private void handleInventoryInsufficientEvent(InventoryInsufficientEvent event) {
        log.warn("재고 부족으로 주문 취소: orderId={}, reason={}, insufficientItems={}", 
                event.getOrderId(), event.getReason(), event.getInsufficientItems().size());
        
        // 주문 취소
        orderService.cancelOrder(event.getOrderId(), event.getReason())
                .doOnSuccess(order -> log.info("주문 취소 완료: orderId={}, status={}", 
                        order.getOrderId(), order.getStatus()))
                .doOnError(error -> log.error("주문 취소 실패: orderId={}, error={}", 
                        event.getOrderId(), error.getMessage()))
                .subscribe();
    }
}