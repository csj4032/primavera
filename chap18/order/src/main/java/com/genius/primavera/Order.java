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

	public Order(String userId, BigDecimal totalAmount) {
		this.userId = userId;
		this.totalAmount = totalAmount;
	}

	@Id
	@Column("ID")
	private Long id;
	
	@Column("ORDER_ID")
	private String orderId;
	
	@Column("USER_ID")
	private String userId;
	
	@Column("CUSTOMER_ID")
	private String customerId;
	
	
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