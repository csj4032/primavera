package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final AccountRepository accountRepository;

	@Override
	public void run(String... args) throws Exception {
		log.info("start data initialization  ... {}");
		accountRepository
				.deleteAll()
				.thenMany(Flux.just(
						new User(1l, "Name1", LocalDateTime.now()),
						new User(2l, "Name2", LocalDateTime.now()),
						new User(3l, "Name3",LocalDateTime.now())).flatMap(user -> accountRepository.save(user)))
				.log()
				.subscribe(null, null, () -> log.info("done initialization..."));
	}
}
