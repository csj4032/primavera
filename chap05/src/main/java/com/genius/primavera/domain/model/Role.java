package com.genius.primavera.domain.model;

import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
	@NotNull
	@Min(value = 1)
	private long id;
	@NotNull
	private RoleType type;
}
