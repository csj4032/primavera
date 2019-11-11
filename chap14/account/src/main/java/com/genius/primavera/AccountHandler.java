package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountHandler {

	private final UserRepository userRepository;

	public Mono<ServerResponse> get(ServerRequest req) {
		return userRepository
				.findById(req.pathVariable("id"))
				.flatMap(user -> ServerResponse.ok().body(Mono.just(user), String.class))
				.switchIfEmpty(ServerResponse.status(202).build());
	}
}
