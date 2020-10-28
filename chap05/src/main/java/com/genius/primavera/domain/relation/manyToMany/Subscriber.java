package com.genius.primavera.domain.relation.manyToMany;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@ToString
@Table(name = "SUBSCRIBER")
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "NAME")
	private String name;
}
