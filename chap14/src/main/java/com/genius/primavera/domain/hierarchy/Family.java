package com.genius.primavera.domain.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@ToString
@Table(name = "family")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
public abstract class Family {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "scientific_name")
	private String scientificName;

	@Column(name = "habitat")
	private String habitat;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
