package com.genius.primavera.domain.model;

import java.time.Instant;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Winner {
	private long id;
	private long userId;
	private WinnerType winner;
	private Instant createdAt;
}