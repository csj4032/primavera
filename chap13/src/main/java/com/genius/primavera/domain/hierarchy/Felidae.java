package com.genius.primavera.domain.model.hierarchy;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "FELIDAE")
public class Felidae extends Family {

	@Column(name = "STRUCTURE")
	private String structure;
}
