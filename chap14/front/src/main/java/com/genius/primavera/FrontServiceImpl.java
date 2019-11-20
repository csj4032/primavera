package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrontServiceImpl implements FrontService {

	private static String ACCOUNT_URL = "http://localhost:8081/accounts/{userId}";
	private static String ORDER_URL = "http://localhost:8082/{userId}/orders";
	private static String PRODUCT_URL = "http://localhost:8083/products/{productIds}";

	private final WebClient.Builder webClient;
	private final RestTemplate restTemplate;

	@Override
	public Mono<FrontOrder> findAllOrdersRx(String userId) {
		log.info("findAllOrdersRx : {}", userId);
		return webClient.build().get().uri(ACCOUNT_URL, userId).retrieve().bodyToMono(User.class)
				.flatMap(user -> {
					Mono<List<Order>> monoOrders = webClient.build().get().uri(ORDER_URL, user.getId()).retrieve().bodyToMono(new ParameterizedTypeReference<List<Order>>() {});
					return monoOrders.map(orders -> {
						log.info(orders.toString());
						orders.stream()
								.map(order -> {
									Mono<Product> monoProduct = webClient.build().get().uri(PRODUCT_URL, order.getProductId()).retrieve().bodyToMono(Product.class);
									monoProduct.map(product -> {
										order.setProduct(new Product());
										log.info(order.toString());
										log.info(product.toString());
										return product;
									}).subscribe();
									return order;
								});
						//Mono<List<Product>> monoProducts = webClient.build().get().uri(PRODUCT_URL, getProductIds(orders)).retrieve().bodyToMono(new ParameterizedTypeReference<List<Product>>() {});
						//monoProducts.map(products -> products.stream().map(product -> orders.stream().map(order -> {
						//	if(order.getProductId() == product.getId()) order.setProduct(product);
						//	return Mono.just(order);
						//})));
						return new FrontOrder(user, orders);
					});
				});
	}

	@Override
	public FrontOrder findAllOrders(String userId) {
		User user = restTemplate.getForObject(ACCOUNT_URL, User.class, userId);
		List<Order> orders =  restTemplate.exchange(ORDER_URL, HttpMethod.GET,null, new ParameterizedTypeReference<List<Order>>() {}, userId).getBody();
		return new FrontOrder(user, orders);
	}

	private String getProductIds(List<Order> orders) {
		return orders.stream().map(order -> String.valueOf(order.getProductId())).collect(Collectors.joining(","));
	}
}