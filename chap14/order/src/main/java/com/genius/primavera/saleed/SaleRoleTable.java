package com.genius.primavera.saleed;

import com.genius.primavera.saleed.role.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SaleRoleTable {

	private static SaleRoleTable discountRoleTable;
	private final Map<SaleRoleType, Saleable> discountableTable = new HashMap<>();

	private SaleRoleTable() {
		discountableTable.put(SaleRoleType.LEGAL, new LegalSaleRole());
		discountableTable.put(SaleRoleType.AMOUNT, new AmountSaleRole());
		discountableTable.put(SaleRoleType.STOCK, new StockSaleRole());
		discountableTable.put(SaleRoleType.EVENT, new EventSaleRole());
	}

	public static SaleRoleTable getInstance() {
		if (discountRoleTable == null) {
			discountRoleTable = new SaleRoleTable();
		}
		return discountRoleTable;
	}
}
