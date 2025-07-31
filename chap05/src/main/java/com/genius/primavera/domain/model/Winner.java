package com.genius.primavera.domain.model;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Winner {
	private Long id;
	private String name;
	private Integer year;
	private String sport;
	private String prize;
	private BigDecimal amount;
}