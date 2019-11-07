package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FrontServiceImpl implements FrontService {

	private static String ACCOUNT_URL = "http://localhost:8081/accounts/{userId}";
	private static String ORDER_URL = "http://localhost:8082/{userId}/orders";

	private final WebClient.Builder webClient;

	@Override
	public Mono<FrontOrder> findAllOrders(String userId) {
		return webClient.build().get().uri(ACCOUNT_URL, userId).retrieve().bodyToMono(User.class)
				.flatMap(user -> {
					Mono<List<Order>> monoOrders = webClient.build().get().uri(ORDER_URL, user.getId()).retrieve().bodyToMono(new ParameterizedTypeReference<List<Order>>() {});
					return monoOrders.map(orders -> new FrontOrder(user, orders));
				});
	}
}