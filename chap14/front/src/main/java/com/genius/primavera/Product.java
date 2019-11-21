package com.genius.primavera;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
	private BigDecimal price = BigDecimal.ZERO;
}

