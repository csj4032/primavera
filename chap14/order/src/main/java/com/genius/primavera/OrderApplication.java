package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Slf4j
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableR2dbcRepositories
public class OrderApplication {

	private static String USERS_USER_ID_ORDER_URL = "users/{userId}/orders";

	public static void main(String[] args) {

		new SpringApplicationBuilder(OrderApplication.class)
				.initializers((GenericApplicationContext context) -> {
					context.registerBean(RouterFunction.class, () -> {
						var orderRepository = context.getBean(OrderRepository.class);
						var orderService = new OrderServiceImpl(orderRepository);
						return route().GET(USERS_USER_ID_ORDER_URL, request -> {
							return ok().body(orderService.findByUserId(request.pathVariable("userId")), Order.class);
						}).build();
					});
				})
				.build()
				.run(args);
	}
}