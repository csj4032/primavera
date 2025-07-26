package com.genius.primavera.domain.model;

import java.time.LocalDateTime;

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
	private LocalDateTime regDt;
}