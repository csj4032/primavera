package com.genius.primavera.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "email"})
public class User {
	private long id;
	private String email;
	private String password;
	private String nickname;
	private UserStatus status;
	private List<Role> roles;
	private LocalDateTime regDate;
	private LocalDateTime modDate;
}