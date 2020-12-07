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

	private final AccountRepository accountRepository;

	public Mono<ServerResponse> get(ServerRequest req) {
		try {
			Thread.sleep(1000);
			log.debug("Sleep!!!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return accountRepository
				.findById(req.pathVariable("id"))
				.flatMap(user -> ServerResponse.ok().body(Mono.just(user), User.class))
				.switchIfEmpty(ServerResponse.status(202).build());
	}
}