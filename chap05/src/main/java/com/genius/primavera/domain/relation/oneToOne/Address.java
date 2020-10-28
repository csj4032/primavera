package com.genius.primavera.domain.relation.oneToOne;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
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
