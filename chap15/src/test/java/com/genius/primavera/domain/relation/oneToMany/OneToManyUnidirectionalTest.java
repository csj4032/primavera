package com.genius.primavera.domain.relation.oneToMany;

import com.genius.primavera.domain.relation.JpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("1:N connection test connection")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OneToManyUnidirectionalTest extends JpaTestBase {

	@Test
	@Order(1)
	@DisplayName("test, test")
	public void save() {
		var student1 = new Student(null, "student1");
		var student2 = new Student(null, "student2");
		var professor = Professor.of("professor");
		professor.getStudents().add(student1);
		professor.getStudents().add(student2);

		entityTransaction.begin();
		entityManager.persist(student1);
		entityManager.persist(student2);
		entityManager.persist(professor);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("test, test inquiry")
	public void find() {
		var student1 = entityManager.find(Student.class, 1l);
		var student2 = entityManager.find(Student.class, 2l);
		var student3 = entityManager.find(Student.class, 3l);
		log.info("student1 : {}", student1);
		log.info("student2 : {}", student2);
		log.info("student3 : {}", student3);
		var professor = entityManager.find(Professor.class, 1l);
		log.info("professor : {}", professor);
	}
}