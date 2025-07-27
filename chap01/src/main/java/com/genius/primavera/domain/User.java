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
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public User() {
	}

	public User(long id) {
		this.id = id;
	}

	public User(long id, String email, String password, String nickname, List<Role> roles, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.roles = roles;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}