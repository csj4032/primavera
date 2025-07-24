package com.genius.primavera;

import reactor.core.publisher.Flux;

public interface OrderService {

	Flux<Order> findByUserId(String userId);
}
