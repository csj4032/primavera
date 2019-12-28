package com.genius.primavera.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
public class Financial {
	private String segment;
	private String country;
	private String product;
	private String discountBand;
	private Double unitsSold;
	private BigDecimal manufacturingPrice;
	private BigDecimal salePrice;
	private BigDecimal grossSales;
	private String discounts;
	private BigDecimal sales;
	private BigDecimal cogs;
	private BigDecimal profit;
	private Instant date;

	public static Financial of(Row row) {
		return Financial.builder()
				.segment(row.getCell(0).getStringCellValue())
				.country(row.getCell(1).getStringCellValue())
				.product(row.getCell(2).getStringCellValue())
				.discountBand(row.getCell(3).getStringCellValue())
				.unitsSold(row.getCell(4).getNumericCellValue())
				.manufacturingPrice(BigDecimal.valueOf(row.getCell(5).getNumericCellValue()))
				.salePrice(BigDecimal.valueOf(row.getCell(6).getNumericCellValue()))
				.grossSales(BigDecimal.valueOf(row.getCell(7).getNumericCellValue()))
				.discounts(row.getCell(8).getStringCellValue())
				.sales(BigDecimal.valueOf(row.getCell(9).getNumericCellValue()))
				.cogs(BigDecimal.valueOf(row.getCell(10).getNumericCellValue()))
				.profit(BigDecimal.valueOf(row.getCell(11).getNumericCellValue()))
				.date(row.getCell(12).getDateCellValue().toInstant())
				.build();
	}
}
