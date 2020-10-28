package com.genius.primavera.domain.relation.oneToOne;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@ToString
@Table(name = "SERIAL")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Serial {

	enum Type {
		Type1,
		Type2
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "NUMBER")
	private String number;

	@NonNull
	@Column(name = "TYPE")
	@Enumerated(value = EnumType.STRING)
	private Type type;

	@OneToOne(mappedBy = "serial")
	private Product product;
}
