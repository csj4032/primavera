package com.genius.primavera.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "temp")
public class Temp {

	private static final long serialVersionUID = 2405172041950251807L;

	@Transient
	public static final String SEQUENCE_NAME = "users_sequence";

	public Temp(Long id) {
		this.id = id;
	}

	@Id
	private Long id;
	@CreatedDate
	private LocalDateTime createDt;
}
