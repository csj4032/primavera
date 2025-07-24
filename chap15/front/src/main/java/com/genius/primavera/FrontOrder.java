package com.genius.primavera;

import lombok.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontOrder {

	public FrontOrder(User user) {
		this.user = user;
	}

	private User user;
	private List<Order> orders;
}
