package com.genius.primavera.saleed.role;

import com.genius.primavera.Order;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegalSaleRole implements Saleable {

	@Override
	public boolean isSaleable(Order order) {
		return false;
	}
}