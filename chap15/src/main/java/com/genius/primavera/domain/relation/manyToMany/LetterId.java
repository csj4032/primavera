package com.genius.primavera.domain.relation.manyToMany;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LetterId implements Serializable {
	private Long sender;
	private Long recipient;
}
