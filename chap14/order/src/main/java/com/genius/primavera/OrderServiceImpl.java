package com.genius.primavera;

import com.genius.primavera.saleed.SaleCommand;
import com.genius.primavera.saleed.SaleRoleTable;
import com.genius.primavera.saleed.SaleRoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;

	@Override
	public Flux<Order> findByUserId(String userId) {
		return orderRepository.findAll();
	}

	private List<Order> newOrderInstance(int n) {
		List<Order> orders = new ArrayList<>();
		for (int i = 1; i <= n; i++) {
			orders.add(new Order(Long.valueOf(i), Long.valueOf(i), Long.valueOf(i), 1000l));
		}
		return orders;
	}
}