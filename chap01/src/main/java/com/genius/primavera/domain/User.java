package com.genius.primavera.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode(of = {"id", "email"})
public class User {
	private long id;
	private String email;
	private String password;
	private String nickname;
	private List<Role> roles;
	private LocalDateTime regDt;
	private LocalDateTime modDt;

	public User() {
	}

	public User(long id) {
		this.id = id;
	}

	public User(long id, String email, String password, String nickname, List<Role> roles, LocalDateTime regDt, LocalDateTime modDt) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.roles = roles;
		this.regDt = regDt;
		this.modDt = modDt;
	}

}