package com.genius.primavera;

import com.genius.primavera.dto.CreateOrderRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

	Flux<Order> findByUserId(String userId);
	
	Mono<Order> createOrder(CreateOrderRequest request);
	
	Mono<Order> cancelOrder(String orderId, String reason);
	
	Mono<Order> confirmInventory(String orderId);
	
	Mono<Order> getOrderById(String orderId);
}
