package com.genius.primavera;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductServiceImpl implements ProductService {

	@Override
	@CacheGet(keyPrefixType = CacheKeyPrefixType.PRODUCT)
	public Product getProduct(@CacheKey(order = 1) long group, @CacheKey(order = 2) long id, String name) {
		return Product.builder().id(id).group(group).name(name).price(BigDecimal.TEN).build();
	}
}
