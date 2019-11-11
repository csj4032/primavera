package com.genius.primavera;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	private long id;
	private String name;
	@CreatedDate
	private LocalDateTime createDate;
}