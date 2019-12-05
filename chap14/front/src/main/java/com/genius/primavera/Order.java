package com.genius.primavera;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	private long id;
	private long productId;
	private Product product;

	protected Order applyProduct(Product p) {
		this.setProduct(p);
		return this;
	}
}