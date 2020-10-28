package com.genius.primavera.domain.relation.oneToMany;

import lombok.*;

import javax.persistence.*;

import java.beans.ConstructorProperties;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@ToString
@Table(name = "TEAM")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Team {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	@NonNull
	@Column(name = "NAME")
	private String name;
}
