package com.genius.primavera;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductServiceImpl implements ProductService {

	@Override
	@CacheGet(keyPrefixType = CacheKeyPrefixType.PRODUCT)
	public Product getProduct(long group, long id) {
		return Product.builder().id(id).group(group).name("product").price(BigDecimal.TEN).build();
	}
}
