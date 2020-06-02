package com.genius.primavera;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	private Long id;
	private Long productId;
}
