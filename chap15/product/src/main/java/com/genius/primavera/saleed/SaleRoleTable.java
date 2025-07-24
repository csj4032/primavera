package com.genius.primavera.saleed;

import com.genius.primavera.saleed.role.*;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Getter
@Service
public class SaleRoleTable {

	private static SaleRoleTable saleRoleTable;
	private final Map<SaleRoleType, Saleable> saleRoleTypeMap = new HashMap<>();

	private SaleRoleTable() {
		saleRoleTypeMap.put(SaleRoleType.LEGAL, new LegalSaleRole());
		saleRoleTypeMap.put(SaleRoleType.AMOUNT, new AmountSaleRole());
		saleRoleTypeMap.put(SaleRoleType.STOCK, new StockSaleRole(500));
		saleRoleTypeMap.put(SaleRoleType.EVENT, new EventSaleRole());
	}

	public static SaleRoleTable getInstance() {
		if (saleRoleTable == null) {
			saleRoleTable = new SaleRoleTable();
		}
		return saleRoleTable;
	}
}
