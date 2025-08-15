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
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrontServiceImpl implements FrontService {

	private final RestTemplate restTemplate;
	private final ServiceUrlConfig serviceUrlConfig;
	private final ParameterizedTypeReference<List<Order>> responseType = new ParameterizedTypeReference<>() {
	};

	@Override
	public FrontOrder findAllOrders(String userId) {
		String accountUrl = serviceUrlConfig.getAccountUrl() + "/users/{userId}";
		String orderUrl = serviceUrlConfig.getOrderUrl() + "/users/{userId}/orders";
		String productUrl = serviceUrlConfig.getProductUrl() + "/products/{productId}";
		
		return new FrontOrder(
				restTemplate.getForObject(accountUrl, User.class, userId),
				Objects.requireNonNull(restTemplate.exchange(orderUrl, HttpMethod.GET, null, responseType, userId).getBody())
						.stream()
						.peek(order -> order.setProduct(restTemplate.getForObject(productUrl, Product.class, order.getProductId())))
						.collect(Collectors.toList()));
	}
}