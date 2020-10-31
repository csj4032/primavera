package com.genius.primavera.domain.model.mapped;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "PROFESSOR")
public class Professor extends BaseEntity {

	@Column(name = "COURSE")
	private String course;
}
