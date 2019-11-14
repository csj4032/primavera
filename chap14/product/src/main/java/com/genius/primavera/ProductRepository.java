package com.genius.primavera;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, Long> {

	Mono<Product> findByIdAndGroupAndName(Long id, Long group, String name);

	Flux<Product> findByName(String name);
}
