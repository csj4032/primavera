package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
@RequiredArgsConstructor
public class OrderRouter {

	private final OrderService orderService;

	@Bean
	RouterFunction<ServerResponse> getUserRoute() {
		return route(GET("/users/{userId}/orders"), req -> {
			try {
				return ok().body(orderService.findByUserId(req.pathVariable("userId")), new ParameterizedTypeReference<List<Order>>(){});
			} catch (InterruptedException e) {
				e.printStackTrace();
				return Mono.error(e);
			}
		});
	}
}
