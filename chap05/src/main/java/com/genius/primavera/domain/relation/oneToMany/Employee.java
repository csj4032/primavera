package com.genius.primavera.domain.relation.oneToMany;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "EMPLOYEE")
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "NAME")
	private String name;

	@ManyToOne
	@JoinColumn(name = "DEPARTMENT_ID")
	// 사원은 부서의 Prime Key 를 Foreign key 등록
	private Department department;

	@Override
	public String toString() {
		return "Employee{" +
				"id=" + id +
				", name='" + name + '\'' +
				", department=" + department.getId() +
				'}';
	}
}
