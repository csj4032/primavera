package com.genius.primavera.domain.relation.oneToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * 양방향은 외래 키가 있는 쪽이 연관관계의 주인
 * 양방향 연관관계는 항상 서로를 참조
 * 무한루프에 빠지므로 주의
 */
@Slf4j
@DisplayName("N:1 사원과 부서 양방향  : 부서는 사원을 참조하는 필드가 있음")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToOneBidirectional {

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
	@DisplayName("부서, 사원 저장")
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
	@DisplayName("부서, 사원 조회")
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
	@DisplayName("사원의 부서 정보 수정")
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
