package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
@RequiredArgsConstructor
public class FrontRouter {

	private final FrontHandler frontHandler;

	@Bean
	protected RouterFunction<ServerResponse> getOrderRoute() {
		return route(GET("/users/{userId}/orders"), req -> ok().body(frontHandler.findAllOrders(req.pathVariable("userId")), FrontOrder.class));
	}
}