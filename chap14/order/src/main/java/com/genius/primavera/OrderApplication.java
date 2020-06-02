package com.genius.primavera;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.List;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootConfiguration
@EnableAutoConfiguration
public class OrderApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(OrderApplication.class)
				.initializers((GenericApplicationContext applicationContext) -> {
					applicationContext.registerBean(RouterFunction.class, () -> {
						return route()
								.GET("/users/{userId}/orders", request -> {
									var userId = request.pathVariable("userId");
									var orderService = applicationContext.getBean("OrderService", OrderService.class);
									var orders = orderService.findByUserId(userId);
									return ServerResponse.ok().body(orders, new ParameterizedTypeReference<List<Order>>() {
									});
								}).build();
					});
				})
				.build()
				.run(args);
	}
}