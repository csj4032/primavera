package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

	@Override
	public Mono<List<Order>> findByUserId(String userId) throws InterruptedException {
		log.info("Start Find Id : {}", userId);
		Thread.sleep(50);
		Mono<List<Order>> orders = Mono.just(List.of(
				Order.builder().id(1l).productId(100l).build(),
				Order.builder().id(2l).productId(99l).build(),
				Order.builder().id(3l).productId(120l).build(),
				Order.builder().id(4l).productId(130l).build(),
				Order.builder().id(5l).productId(110l).build()
		));
		log.info("Stop Find Id : {}", userId);
		return orders;
	}
}