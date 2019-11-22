package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrontServiceImpl implements FrontService {

	private static String ACCOUNT_URL = "http://localhost:8081/accounts/{userId}";
	private static String ORDER_URL = "http://localhost:8082/users/{userId}/orders";
	private static String PRODUCT_URL = "http://localhost:8083/products/{productId}";

	private final WebClient.Builder webClient;
	private final RestTemplate restTemplate;
	private final ParameterizedTypeReference<List<Order>> responseType = new ParameterizedTypeReference<>() {};

	@Override
	public Mono<FrontOrder> findAllOrdersRx(String userId) {
		Mono<User> userMono = webClient.build().get().uri(ACCOUNT_URL, userId).retrieve().bodyToMono(User.class);
		Flux<Order> orderFlux = webClient.build().get().uri(ORDER_URL, userMono).retrieve().bodyToFlux(Order.class).cache();
		Flux<Product> productFlux = orderFlux.flatMap(order -> webClient.build().get().uri(PRODUCT_URL, order.getProductId()).retrieve().bodyToMono(Product.class)).cache();
		return userMono.zipWhen(user -> orderFlux.zipWith(productFlux, Order::applyProduct).collectList()).map(t -> new FrontOrder(t.getT1(), t.getT2()));
	}

	@Override
	public FrontOrder findAllOrders(String userId) {
		return new FrontOrder(
				restTemplate.getForObject(ACCOUNT_URL, User.class, userId),
				restTemplate.exchange(ORDER_URL, HttpMethod.GET, null, responseType, userId).getBody()
						.stream()
						.peek(order -> order.setProduct(restTemplate.getForObject(PRODUCT_URL, Product.class, order.getProductId())))
						.collect(Collectors.toList()));
	}

	private String getProductIds(List<Order> orders) {
		return orders.stream().map(order -> String.valueOf(order.getProductId())).collect(Collectors.joining(","));
	}
}