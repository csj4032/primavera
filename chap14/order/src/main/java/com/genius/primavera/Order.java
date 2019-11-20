package com.genius.primavera;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	private long id;
	private long productId;
}
