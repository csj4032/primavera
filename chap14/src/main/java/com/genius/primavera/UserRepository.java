package com.genius.primavera;

import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.BodyInserter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserRepository {

    static Map<Long, User> userData;
    static Map<Long, String> userDataAccessData;

    static {
        userData = new HashMap<>();
        userData.put(1L, new User(1L, "User 1"));
        userData.put(2L, new User(2L, "User 2"));
        userData.put(3L, new User(3L, "User 3"));
        userData.put(4L, new User(4L, "User 4"));
        userData.put(5L, new User(5L, "User 5"));
        userData.put(6L, new User(6L, "User 6"));
        userData.put(7L, new User(7L, "User 7"));
        userData.put(8L, new User(8L, "User 8"));
        userData.put(9L, new User(9L, "User 9"));
        userData.put(10L, new User(10L, "User 10"));

        userDataAccessData = new HashMap<>();
        userDataAccessData.put(1L, "User 1 Access Key");
        userDataAccessData.put(2L, "User 2 Access Key");
        userDataAccessData.put(3L, "User 3 Access Key");
        userDataAccessData.put(4L, "User 4 Access Key");
        userDataAccessData.put(5L, "User 5 Access Key");
        userDataAccessData.put(6L, "User 6 Access Key");
        userDataAccessData.put(7L, "User 7 Access Key");
        userDataAccessData.put(8L, "User 8 Access Key");
        userDataAccessData.put(9L, "User 9 Access Key");
        userDataAccessData.put(10L, "User 10Access Key");
    }

    public Flux<User> findAllUsers() {
        return Flux.fromIterable(userData.values());
    }

    public Mono<User> findById(String id) {
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
