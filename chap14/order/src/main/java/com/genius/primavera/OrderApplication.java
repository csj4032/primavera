package com.genius.primavera;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.util.List;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootConfiguration
@EnableAutoConfiguration
public class OrderApplication {

	private static String USERS_USER_ID_ORDER_URL = "users/{userId}/orders";
	private static ParameterizedTypeReference<List<Order>> ORDERS_TYPE_REF = new ParameterizedTypeReference<>() {
	};

	public static void main(String[] args) {
		new SpringApplicationBuilder(OrderApplication.class).initializers((GenericApplicationContext context) -> context.registerBean(RouterFunction.class, () -> route().GET(USERS_USER_ID_ORDER_URL, request -> ok().body(context.getBean(OrderService.class).findByUserId(request.pathVariable("userId")), ORDERS_TYPE_REF)).build())).build().run(args);
	}
}