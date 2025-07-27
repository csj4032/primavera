package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AccountRepository {

	public final static String USER_CACHE_KEY = "users";
	private final ReactiveRedisTemplate<String, User> reactiveRedisTemplate;

	Mono<User> save(User user) {
		return reactiveRedisTemplate.<String, User>opsForHash().put(USER_CACHE_KEY, String.valueOf(user.getId()), user).log().map(p -> user);
	}

	Mono<User> findById(String id) {
		return reactiveRedisTemplate.<String, User>opsForHash().get(USER_CACHE_KEY, id);
	}

	Mono<Boolean> deleteAll() {
		return reactiveRedisTemplate.<String, String>opsForHash().delete(USER_CACHE_KEY);
	}
}