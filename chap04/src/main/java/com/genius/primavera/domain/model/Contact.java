package com.genius.primavera.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "email"})
public class Contact {
	private long id;
	private long userId;
	private String email;
	private LocalDateTime regDate;
	private LocalDateTime modDate;
}
