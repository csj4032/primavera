package com.genius.primavera.domain.model;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {
	private long userId;
	private int roleId;
}
