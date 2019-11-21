package com.genius.primavera;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface FrontService {

	Mono<FrontOrder> findAllOrdersRx(String userId);

	FrontOrder findAllOrders(String userId);
}
