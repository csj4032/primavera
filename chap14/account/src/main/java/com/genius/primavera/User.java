package com.genius.primavera;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("USER")
public class User {
	@Id
	private long id;
	private String name;
	@CreatedDate
	private LocalDateTime createDate;
}