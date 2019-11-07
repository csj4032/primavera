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
		Thread.sleep(1000);
		Mono<List<Order>> orders = Mono.just(List.of(
				Order.builder().id(1l).build(),
				Order.builder().id(2l).build(),
				Order.builder().id(3l).build()
		));
		log.info("Stop Find Id : {}", userId);
		return orders;
	}
}
