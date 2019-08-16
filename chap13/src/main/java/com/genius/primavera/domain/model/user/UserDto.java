package com.genius.primavera.domain.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class UserDto {
	private Long id;
	private String email;
	private String nickname;
	private UserStatus status;
}