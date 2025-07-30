package com.genius.primavera.domain.relation.oneToOne;

import lombok.*;

import jakarta.persistence.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity(name = "RelationAddress")
@Getter
@Setter
@ToString
@Table(name = "ADDRESS")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Address {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "VALUE")
	private String value;
}
