package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class AccountRouter {

	private final AccountHandler accountHandler;

	@Bean
	protected RouterFunction<ServerResponse> getAccountRouteRedis() {
		return route(GET("/accounts/{id}"), accountHandler::get);
	}
}