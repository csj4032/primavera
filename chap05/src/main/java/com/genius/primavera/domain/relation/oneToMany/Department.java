package com.genius.primavera.domain.relation.oneToMany;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "DEPARTMENT")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Department {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "NAME")
	private String name;

	@OneToMany(mappedBy = "department")
	private List<Employee> employees = new ArrayList<>();

	public void addEmployee(Employee employee) {
		this.employees.add(employee);
		// 무한루프 주의
		if (employee.getDepartment() != this) {
			employee.setDepartment(this);
		}
	}

	@Override
	public String toString() {
		return "Department{" +
				"id=" + id +
				", name='" + name + '\'' +
				", employees=" + employees +
				'}';
	}
}
