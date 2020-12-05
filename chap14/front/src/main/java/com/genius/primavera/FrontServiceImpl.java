package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrontServiceImpl implements FrontService {

	private static final String ACCOUNT_URL = "http://localhost:8081/accounts/{userId}";
	private static final String ORDER_URL = "http://localhost:8082/users/{userId}/orders";
	private static final String PRODUCT_URL = "http://localhost:8083/products/{productId}";

	private final RestTemplate restTemplate;
	private final ParameterizedTypeReference<List<Order>> responseType = new ParameterizedTypeReference<>() {
	};

	@Override
	public FrontOrder findAllOrders(String userId) {
		return new FrontOrder(
				restTemplate.getForObject(ACCOUNT_URL, User.class, userId),
				restTemplate.exchange(ORDER_URL, HttpMethod.GET, null, responseType, userId).getBody()
						.stream()
						.peek(order -> order.setProduct(restTemplate.getForObject(PRODUCT_URL, Product.class, order.getProductId())))
						.collect(Collectors.toList()));
	}
}