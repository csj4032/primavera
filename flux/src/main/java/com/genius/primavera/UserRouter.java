package com.genius.primavera;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class UserRouter {

    @Bean
    UserRepository userRepository() {
        return new UserRepository();
    }

    @Bean
    RouterFunction<ServerResponse> getUserRoute(UserRepository userRepository) {
        return route(GET("/users"), req -> ok().body(userRepository.findAllUsers(), User.class))
                .andRoute(GET("/users/{id}"), req -> ok().body(userRepository.findById(req.pathVariable("id")), User.class))
                .andRoute(POST("/user"), new HandlerFunction() {
                    @Override
                    public Mono<ServerResponse> handle(ServerRequest request) {
                        Mono<User> user = request.bodyToMono(User.class);
                        return ok().build(userRepository.save(user));
                    }
                });
    }
}