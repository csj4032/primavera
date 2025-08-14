package com.genius.primavera;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "ORDER_ITEMS")
public class OrderItem {

	@Id
	@Column("ID")
	private Long id;
	
	@Column("ORDER_ID")
	private Long orderId;
	
	@Column("PRODUCT_ID")
	private String productId;
	
	@Column("QUANTITY")
	private Integer quantity;
	
	@Column("UNIT_PRICE")
	private BigDecimal unitPrice;
	
	@Column("TOTAL_PRICE")
	private BigDecimal totalPrice;
	
	@Column("CREATED_AT")
	private LocalDateTime createdAt;
}