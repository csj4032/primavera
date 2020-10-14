package com.genius.primavera.saleed;

import com.genius.primavera.Product;
import com.genius.primavera.saleed.role.Saleable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class SaleCommand {

	private final Map<SaleRoleType, Saleable> saleRoleTypeMap;

	public SaleCommand(SaleRoleTable saleRoleTable) {
		this.saleRoleTypeMap = saleRoleTable.getSaleRoleTypeMap();
	}

	public boolean isSaleable(final Product product, Set<SaleRoleType> discountRoleTypes) {
		return discountRoleTypes.stream().map(r -> saleRoleTypeMap.get(r).isSaleable(product)).count() == discountRoleTypes.size();
	}
}