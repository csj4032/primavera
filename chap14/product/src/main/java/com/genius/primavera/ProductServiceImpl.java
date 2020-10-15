package com.genius.primavera;

import com.genius.primavera.cache.CacheGet;
import com.genius.primavera.cache.CacheKey;
import com.genius.primavera.cache.CacheKeyPrefixType;
import com.genius.primavera.saleed.SaleCommand;
import com.genius.primavera.saleed.SaleRoleType;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final SaleCommand saleCommand;

	@Override
	public Flux<Product> findAll() {
		return productRepository.findAll();
	}

	@Override
	@CacheGet(keyPrefixType = CacheKeyPrefixType.PRODUCT)
	public Mono<Product> findByIdAndGroupAndName(@CacheKey(order = 1) long group, @CacheKey(order = 2) long id, String name) {
		return productRepository.findByIdAndGroupAndName(id, group, name);
	}

	@Override
	public Mono<Product> save(Product product) {
		return productRepository.save(product);
	}

	@Override
	public Flux<Product> saveAll(List<Product> products) {
		return productRepository.saveAll(products);
	}

	@Override
	public Mono<Product> findById(Long id) {
		return productRepository.findById(id)
				.filter(p -> saleCommand.isSaleable(p, EnumSet.of(SaleRoleType.LEGAL, SaleRoleType.STOCK)));
	}

	@Override
	public Flux<Product> findByName(String name) {
		return productRepository.findByName(name);
	}

	@Override
	public Mono<Void> deleteAll() {
		return productRepository.deleteAll();
	}
}
