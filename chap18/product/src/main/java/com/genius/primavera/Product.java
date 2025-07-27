package com.genius.primavera;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@Document(collection = "product")
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	@Id
	@Field(name = "id")
	private Long id;
	private Long group;
	private String name;
	@Builder.Default
	private BigDecimal price = BigDecimal.ZERO;
	private Long stock;
	private Instant createDate;
}
