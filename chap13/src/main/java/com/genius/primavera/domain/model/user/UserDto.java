package com.genius.primavera.domain.model.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
	private Long id;
	private String email;
	private String password;
	private String nickname;
	private UserStatus status;
}
