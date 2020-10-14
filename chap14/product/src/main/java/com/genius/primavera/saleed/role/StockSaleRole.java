package com.genius.primavera.saleed.role;

import com.genius.primavera.Product;

public class StockSaleRole implements Saleable {

	private long min;

	public StockSaleRole(long min) {
		this.min = min;
	}

	@Override
	public boolean isSaleable(Product product) {
		return product.getStock() > min;
	}
}
