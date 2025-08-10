package com.genius.primavera.domain.relation.manyToOne;

import com.genius.primavera.domain.relation.JpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("N:1 translated_text_3 translated_text_2 translated_text_3  : translated_text_2 translated_text_3 translated_text_4 translated_text_3 translated_text_2")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToOneBidirectionalTest extends JpaTestBase {

	@Test
	@Order(1)
	@DisplayName("translated_text_2, translated_text_2 translated_text_2")
	public void save() {
		var department = Department.of("Department1");

		var employee1 = new Employee(null, "employee1", null);
		var employee2 = new Employee(null, "employee1", null);
		var employee3 = new Employee(null, "employee1", null);

		department.addEmployee(employee1);
		department.addEmployee(employee2);
		department.addEmployee(employee3);

		entityTransaction.begin();
		entityManager.persist(employee1);
		entityManager.persist(employee2);
		entityManager.persist(employee3);
		entityManager.persist(department);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("translated_text_2, translated_text_2 inquiry")
	public void find() {
		var employee1 = entityManager.find(Employee.class, 1l);
		var employee2 = entityManager.find(Employee.class, 2l);
		var employee3 = entityManager.find(Employee.class, 3l);
		log.info("employee1 : {}", employee1);
		log.info("employee1 : {}", employee2);
		log.info("employee1 : {}", employee3);
		var department = entityManager.find(Department.class, 1l);
		log.info("department : {}", department);
	}

	@Test
	@Order(3)
	@DisplayName("translated_text_2 translated_text_2 information modification")
	public void employeeUpdate() {
		var employee1 = entityManager.find(Employee.class, 1l);
		var department = Department.of("department2");
		employee1.setDepartment(department);
		entityTransaction.begin();
		entityManager.persist(department);
		entityManager.persist(employee1);
		entityTransaction.commit();
	}
}
