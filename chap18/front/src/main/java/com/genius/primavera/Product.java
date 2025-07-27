package com.genius.primavera;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	private Long id;
	private Long group;
	private String name;
	@Builder.Default
	private BigDecimal price = BigDecimal.ZERO;
}