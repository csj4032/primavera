package com.genius.primavera;

import reactor.core.publisher.Mono;

import java.util.List;

public interface FrontService {

	Mono<FrontOrder> findAllOrders(String userId);
}
