package com.genius.primavera.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = {"id", "name"})
public class User {
	private long id;
	private String name;
	private List<Contact> contacts;
	private LocalDateTime regDate;
	private LocalDateTime modDate;
}
