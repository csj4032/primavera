package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UserRepository {

	static Map<Long, User> userData;
	static Map<Long, String> userDataAccessData;
	static {
		userData = new HashMap<>();
		userDataAccessData = new HashMap<>();
		for (Long i = 1L; i < 101; i++) {
			userData.put(i, new User(i, "User " + i, LocalDateTime.now()));
			userDataAccessData.put(i, "User " + i + " Access Key");
		}
	}

	public Flux<User> findAllUsers() {
		return Flux.fromIterable(userData.values());
	}

	public Mono<User> findById(String id) {
		log.info("Find Id : {}", id);
		return Mono.just(userData.get(Long.valueOf(id)));
	}

	public Mono<Void> save(Mono<User> user) {
		return user.doOnNext(u -> {
			long id = userData.size() + 1;
			userData.put(id, u);
			System.out.format("Saved %s with id %d%n", u, id);
		}).thenEmpty(Mono.empty());
	}
}
