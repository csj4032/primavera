package com.genius.primavera;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductService {

	Flux<Product> findAll();

	Mono<Product> findByIdAndGroupAndName(long id, long group, String name);

	Mono<Product> save(Product build);

	Flux<Product> saveAll(List<Product> products);

	Mono<Product> findById(Long id);

	Flux<Product> findByName(String name);

	Mono<Void> deleteAll();
}
