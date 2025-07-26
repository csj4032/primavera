package com.genius.primavera.domain.model.mapped;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "PROFESSOR")
public class Professor extends BaseEntity {

	@Column(name = "COURSE")
	private String course;
}
