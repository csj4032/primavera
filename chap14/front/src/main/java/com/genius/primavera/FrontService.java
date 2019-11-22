package com.genius.primavera;

import reactor.core.publisher.Mono;

public interface FrontService {

	Mono<FrontOrder> findAllOrdersRx(String userId);

	FrontOrder findAllOrders(String userId);
}
