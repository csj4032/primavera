package com.genius.primavera.domain.model.mapped;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "STUDENT")
public class Student extends BaseEntity {

	@Column(name = "DEPARTMENT")
	private String department;
}
