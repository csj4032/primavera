package com.genius.primavera.saleed;

import com.genius.primavera.saleed.role.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component
public class SaleRoleTable {

	private final Map<SaleRoleType, Saleable> discountableTable = new HashMap<>();

	public SaleRoleTable() {
		discountableTable.put(SaleRoleType.LEGAL, new LegalSaleRole());
		discountableTable.put(SaleRoleType.AMOUNT, new AmountSaleRole());
		discountableTable.put(SaleRoleType.STOCK, new StockSaleRole());
		discountableTable.put(SaleRoleType.EVENT, new EventSaleRole());
	}
}
