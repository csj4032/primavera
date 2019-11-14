package com.genius.primavera;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@Document(collection = "product")
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	@Id
	private Long id;
	private Long group;
	private String name;
	private BigDecimal price = BigDecimal.ZERO;
	private LocalDateTime createDate;
}
