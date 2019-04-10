package com.genius.primavera.domain.model;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
