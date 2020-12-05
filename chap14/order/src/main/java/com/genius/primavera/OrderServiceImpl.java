package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;

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

	private List<Order> newOrderInstance(int n) {
		List<Order> orders = new ArrayList<>();
		for (int i = 1; i <= n; i++) {
			orders.add(new Order(Long.valueOf(i), Long.valueOf(i), Long.valueOf(i), 1000l));
		}
		return orders;
	}
}