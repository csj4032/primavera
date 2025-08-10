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
	
	@Column("ORDER_ID")
	private String orderId;
	
	@Column("USER_ID")
	private Long userId;
	
	@Column("CUSTOMER_ID")
	private String customerId;
	
	@Column("PRODUCT_ID")
	private Long productId;
	
	@Column("AMOUNT")
	private Long amount;
	
	@Column("TOTAL_AMOUNT")
	private BigDecimal totalAmount;
	
	@Column("STATUS")
	@Builder.Default
	private OrderStatus status = OrderStatus.PENDING;
	
	@Column("CREATED_AT")
	private LocalDateTime createdAt;
	
	@Column("UPDATED_AT")
	private LocalDateTime updatedAt;
	
	public enum OrderStatus {
		PENDING,
		INVENTORY_CONFIRMED,
		CANCELLED,
		COMPLETED
	}
}