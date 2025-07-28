package com.genius.primavera;

import com.genius.primavera.cache.CacheGet;
import com.genius.primavera.cache.CacheKey;
import com.genius.primavera.cache.CacheKeyPrefixType;
import com.genius.primavera.dto.InventoryReservationResult;
import com.genius.primavera.event.*;
import com.genius.primavera.saleed.SaleCommand;
import com.genius.primavera.saleed.SaleRoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final SaleCommand saleCommand;
	private final InventoryEventPublisher eventPublisher;

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
	
	/**
	 * 주문 생성 이벤트에 대한 재고 처리
	 */
	public Mono<InventoryReservationResult> processInventoryReservation(OrderCreatedEvent event) {
		log.info("재고 예약 처리 시작: orderId={}, items={}", event.getOrderId(), event.getItems().size());
		
		return checkAndReserveInventory(event.getItems())
				.flatMap(result -> {
					if (result.isSuccess()) {
						// 재고 예약 성공 이벤트 발행
						return eventPublisher.publishInventoryReservedEvent(event.getOrderId(), event.getItems())
								.thenReturn(result);
					} else {
						// 재고 부족 이벤트 발행
						return eventPublisher.publishInventoryInsufficientEvent(
								event.getOrderId(), 
								result.getInsufficientItems(), 
								result.getReason())
								.thenReturn(result);
					}
				});
	}
	
	/**
	 * 재고 확인 및 차감
	 */
	private Mono<InventoryReservationResult> checkAndReserveInventory(List<OrderItemEvent> items) {
		List<InsufficientItemEvent> insufficientItems = new ArrayList<>();
		
		return Flux.fromIterable(items)
				.flatMap(item -> {
					Long productId = Long.valueOf(item.getProductId());
					return productRepository.findById(productId)
							.flatMap(product -> {
								if (product.getStock() >= item.getQuantity()) {
									// 재고 차감
									product.setStock(product.getStock() - item.getQuantity());
									return productRepository.save(product)
											.doOnSuccess(savedProduct -> 
												log.info("재고 차감 완료: productId={}, quantity={}, remainingStock={}", 
														productId, item.getQuantity(), savedProduct.getStock()))
											.thenReturn(true);
								} else {
									// 재고 부족
									insufficientItems.add(InsufficientItemEvent.builder()
											.productId(item.getProductId())
											.requestedQuantity(item.getQuantity())
											.availableQuantity(product.getStock().intValue())
											.build());
									
									log.warn("재고 부족: productId={}, requested={}, available={}", 
											productId, item.getQuantity(), product.getStock());
									
									return Mono.just(false);
								}
							})
							.switchIfEmpty(Mono.fromRunnable(() -> {
								// 상품이 존재하지 않는 경우
								insufficientItems.add(InsufficientItemEvent.builder()
										.productId(item.getProductId())
										.requestedQuantity(item.getQuantity())
										.availableQuantity(0)
										.build());
								
								log.warn("상품을 찾을 수 없음: productId={}", productId);
							}).thenReturn(false));
				})
				.collectList()
				.map(results -> {
					if (insufficientItems.isEmpty()) {
						return InventoryReservationResult.success();
					} else {
						return InventoryReservationResult.failure(
								"재고가 부족하거나 상품을 찾을 수 없습니다", 
								insufficientItems);
					}
				});
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
