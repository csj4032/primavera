package com.genius.primavera.domain.model;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
	private long id;
	private RoleType type;
}
