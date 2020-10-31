package com.genius.primavera.domain.model.mapped;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "STUDENT")
public class Student extends BaseEntity {

	@Column(name = "DEPARTMENT")
	private String department;
}
