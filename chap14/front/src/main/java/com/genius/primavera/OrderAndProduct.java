package com.genius.primavera;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OrderAndProduct implements Serializable {

	private final Order order;
	private final Product product;

	public OrderAndProduct(Order order, Product product) {
		this.order = order;
		this.product = product;
	}
}
