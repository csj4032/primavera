package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
@RequiredArgsConstructor
public class AccountRouter {

	private final UserRepository userRepository;
	private final AccountHandler accountHandler;

	@Bean
	RouterFunction<ServerResponse> getAccountRoute() {
		return route(GET("/accounts/{id}"), req -> ok().body(userRepository.findById(req.pathVariable("id")), User.class));
	}

	@Bean
	RouterFunction<ServerResponse> getAccountRouteRedis() {
		return route(GET("/redis/accounts/{id}"), accountHandler::get);
	}
}