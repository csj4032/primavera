package com.genius.primavera.saleed.role;

import com.genius.primavera.Product;

import java.util.List;
import java.util.function.Predicate;

public class StockSaleRole implements Saleable {

	private long minimum;

	public StockSaleRole(long minimum) {
		this.minimum = minimum;
	}

	@Override
	public boolean isSaleable(Product product) {
		return product.getStock() > minimum;
	}
}