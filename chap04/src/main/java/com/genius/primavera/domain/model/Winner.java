package com.genius.primavera.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Winner {
	private long id;
	private long userId;
	private String eventDt;
	private char winnerYn = 'N';
	private LocalDateTime regDt;
}
