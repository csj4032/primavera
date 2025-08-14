package com.genius.primavera;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

	Flux<Order> findByUserId(String userId);
	
	Mono<Order> findByOrderId(String orderId);
}
