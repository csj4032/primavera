package com.genius.primavera;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "ORDERS")
public class Order {

	public Order(Long userId, Long productId, Long amount) {
		this.userId = userId;
		this.productId = productId;
		this.amount = amount;
	}

	@Id
	@Column("ID")
	private Long id;
	@Column("USER_ID")
	private Long userId;
	@Column("PRODUCT_ID")
	private Long productId;
	@Column("AMOUNT")
	private Long amount;
}