package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PostConstruct
	public void init() throws InterruptedException {
		productService.deleteAll().subscribe();
		List<Product> products = new ArrayList<>();
		for (Long i = 0L; i < 1000L; i++)
			products.add(Product.builder().id(i).group(i).name("product" + i).price(i % 2 == 0 ? BigDecimal.TEN : BigDecimal.ONE).createDate(LocalDateTime.now()).build());
		productService.saveAll(products).subscribe();
	}

	@GetMapping("/products")
	public Flux<Product> findAll() {
		return productService.findAll();
	}

	@GetMapping("/products/{id:[\\d]+}")
	public Mono<Product> findById(@PathVariable("id") Long id) {
		return productService.findById(id);
	}

	@GetMapping("/products/{name:^[a-zA-F]{1,100}$}")
	public Flux<Product> findByName(@PathVariable("name") String name) {
		return productService.findByName(name);
	}
}
