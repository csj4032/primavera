package com.genius.primavera.saleed.role;

import com.genius.primavera.Order;

public class StockSaleRole implements Saleable {

	@Override
	public boolean isSaleable(Order order) {
		return false;
	}
}
