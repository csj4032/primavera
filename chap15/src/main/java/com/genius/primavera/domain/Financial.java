package com.genius.primavera.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
	private BigDecimal COGS;
	private BigDecimal profit;
	private Instant date;
}
