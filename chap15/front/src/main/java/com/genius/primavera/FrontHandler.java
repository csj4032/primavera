package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FrontHandler {

	private static final String ACCOUNT_URL = "http://localhost:8081/accounts/{userId}";
	private static final String ORDER_URL = "http://localhost:8082/users/{userId}/orders";
	private static final String PRODUCT_URL = "http://localhost:8083/products/{productId}";

	private final WebClient.Builder webClient;

	public Mono<FrontOrder> findAllOrders(String userId) {
		Mono<User> userMono = webClient.build().get().uri(ACCOUNT_URL, userId).retrieve().bodyToMono(User.class);
		Flux<Order> orderFlux = webClient.build().get().uri(ORDER_URL, userId).retrieve().bodyToFlux(Order.class)
				.flatMap(order -> {
					Mono<Product> productMono = webClient.build().get().uri(PRODUCT_URL, order.getProductId()).retrieve().bodyToMono(Product.class);
					return productMono.map(product -> new Order(order.getId(), order.getProductId(), product));
				});
		return Mono.zip(userMono, orderFlux.collectList()).map(t -> new FrontOrder(t.getT1(), t.getT2()));
	}
}
