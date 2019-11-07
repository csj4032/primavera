package com.genius.primavera;

import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {

	Mono<List<Order>> findByUserId(String userId) throws InterruptedException;
}
