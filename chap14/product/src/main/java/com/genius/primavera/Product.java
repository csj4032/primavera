package com.genius.primavera;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	private long id;
	private long group;
	private String name;
	private BigDecimal price = BigDecimal.ZERO;
}
