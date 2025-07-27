package com.genius.primavera.domain;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

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

	public long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getNickname() {
		return nickname;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public LocalDateTime getRegDt() {
		return createdAt;
	}

	public LocalDateTime getModDt() {
		return updatedAt;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public void setRegDt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setModDt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
