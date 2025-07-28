package com.genius.primavera;

import com.genius.primavera.dto.CreateOrderRequest;
import com.genius.primavera.event.OrderCreatedEvent;
import com.genius.primavera.event.OrderEventPublisher;
import com.genius.primavera.event.OrderItemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final OrderEventPublisher eventPublisher;

	@Override
	public Flux<Order> findByUserId(String userId) {
		try {
			Thread.sleep(0);
			log.debug("Sleep!!!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return orderRepository.findByUserId(Long.valueOf(userId));
	}
	
	public Mono<Order> createOrder(CreateOrderRequest request) {
		return Mono.fromCallable(() -> {
			// 주문 ID 생성
			String orderId = "ORD-" + System.currentTimeMillis();
			
			// 총액 계산
			BigDecimal totalAmount = request.getItems().stream()
					.map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			// 주문 엔티티 생성
			Order order = Order.builder()
					.orderId(orderId)
					.customerId(request.getCustomerId())
					.totalAmount(totalAmount)
					.status(Order.OrderStatus.PENDING)
					.createdAt(LocalDateTime.now())
					.updatedAt(LocalDateTime.now())
					.build();
			
			return order;
		})
		.flatMap(orderRepository::save)
		.flatMap(savedOrder -> {
			// Kafka 이벤트 발행
			OrderCreatedEvent event = createOrderCreatedEvent(savedOrder, request);
			return eventPublisher.publishOrderCreatedEvent(event)
					.thenReturn(savedOrder);
		})
		.doOnSuccess(order -> log.info("주문 생성 완료: orderId={}, customerId={}, totalAmount={}", 
				order.getOrderId(), order.getCustomerId(), order.getTotalAmount()))
		.doOnError(error -> log.error("주문 생성 실패: customerId={}, error={}", 
				request.getCustomerId(), error.getMessage()));
	}
	
	public Mono<Order> cancelOrder(String orderId, String reason) {
		return orderRepository.findByOrderId(orderId)
				.switchIfEmpty(Mono.error(new RuntimeException("주문을 찾을 수 없습니다: " + orderId)))
				.flatMap(order -> {
					order.setStatus(Order.OrderStatus.CANCELLED);
					order.setUpdatedAt(LocalDateTime.now());
					return orderRepository.save(order);
				})
				.flatMap(cancelledOrder -> {
					// 주문 취소 이벤트 발행
					return eventPublisher.publishOrderCancelledEvent(orderId, reason)
							.thenReturn(cancelledOrder);
				})
				.doOnSuccess(order -> log.info("주문 취소 완료: orderId={}, reason={}", orderId, reason))
				.doOnError(error -> log.error("주문 취소 실패: orderId={}, error={}", orderId, error.getMessage()));
	}
	
	public Mono<Order> confirmInventory(String orderId) {
		return orderRepository.findByOrderId(orderId)
				.switchIfEmpty(Mono.error(new RuntimeException("주문을 찾을 수 없습니다: " + orderId)))
				.flatMap(order -> {
					order.setStatus(Order.OrderStatus.INVENTORY_CONFIRMED);
					order.setUpdatedAt(LocalDateTime.now());
					return orderRepository.save(order);
				})
				.doOnSuccess(order -> log.info("재고 확인 완료: orderId={}", orderId));
	}
	
	public Mono<Order> getOrderById(String orderId) {
		return orderRepository.findByOrderId(orderId)
				.switchIfEmpty(Mono.error(new RuntimeException("주문을 찾을 수 없습니다: " + orderId)));
	}
	
	private OrderCreatedEvent createOrderCreatedEvent(Order order, CreateOrderRequest request) {
		List<OrderItemEvent> items = request.getItems().stream()
				.map(item -> OrderItemEvent.builder()
						.productId(item.getProductId())
						.productName(item.getProductName())
						.quantity(item.getQuantity())
						.unitPrice(item.getUnitPrice())
						.totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
						.build())
				.collect(Collectors.toList());
		
		return OrderCreatedEvent.builder()
				.orderId(order.getOrderId())
				.customerId(order.getCustomerId())
				.items(items)
				.totalAmount(order.getTotalAmount())
				.build();
	}
}