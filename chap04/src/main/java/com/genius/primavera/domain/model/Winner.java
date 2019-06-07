package com.genius.primavera.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Winner {
	private long id;
	private long userId;
	private WinnerType winner;
	private LocalDateTime regDt;
}