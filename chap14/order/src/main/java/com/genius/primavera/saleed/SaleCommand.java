package com.genius.primavera.saleed;

import com.genius.primavera.Order;
import com.genius.primavera.saleed.role.Saleable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class SaleCommand {

	private final Map<SaleRoleType, Saleable> discountableMap;

	public SaleCommand(SaleRoleTable discountRoleTable) {
		this.discountableMap = discountRoleTable.getDiscountableTable();
	}

	public boolean isSaleable(final Order order, Set<SaleRoleType> discountRoleTypes) {
		return discountRoleTypes.stream().map(r -> discountableMap.get(r).isSaleable(order)).count() == discountRoleTypes.size();
	}
}