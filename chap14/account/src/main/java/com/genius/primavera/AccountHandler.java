package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.genius.primavera.AccountRepository.USER_CACHE_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountHandler {

	private final ReactiveRedisOperations<String, String> reactiveRedisTemplate;

	public Mono<ServerResponse> get(ServerRequest req) {
		return this.reactiveRedisTemplate.<String, String>opsForHash().get(USER_CACHE_KEY, req.pathVariable("id"))
				.flatMap(user -> ServerResponse.ok().body(Mono.just(user), String.class))
				.switchIfEmpty(ServerResponse.status(202).build());
	}
}
