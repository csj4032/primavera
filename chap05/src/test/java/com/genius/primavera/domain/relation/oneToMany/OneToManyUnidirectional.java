package com.genius.primavera.domain.relation.oneToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * 단점
 * 1. 외래 키가 다른 테이블에 있는 점 (연관관계 처리를 위한 UPDATE SEQ, 성능이슈)
 * 2. 다대일 양방향을 권장
 */
@Slf4j
@DisplayName("1:N 교수와 학생 단방향")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OneToManyUnidirectional {

	private static EntityManagerFactory entityManagerFactory;
	private static EntityManager entityManager;
	private static EntityTransaction entityTransaction;

	@BeforeAll
	public static void setUp() {
		entityManagerFactory = Persistence.createEntityManagerFactory("basic");
		entityManager = entityManagerFactory.createEntityManager();
		entityTransaction = entityManager.getTransaction();
	}

	@Test
	@Order(1)
	@DisplayName("교수, 학생 저장")
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
	@DisplayName("교수, 학생 조회")
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