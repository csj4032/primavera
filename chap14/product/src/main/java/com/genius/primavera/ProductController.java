package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping("/products/{group}/{id}")
	public Product getProduct(@PathVariable(name = "group") long group, @PathVariable("id") long id) {
		return productService.getProduct(group, id);
	}
}
