package com.genius.primavera.domain.relation.oneToOne;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * 객체의 참조는 User 가지고 실제 테이블의 외래키는 Address 에 존재 하도록 매핑 할 수 없음
 */
@Slf4j
@DisplayName("1:1 대상 테이블에 외래 키 단방향")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SharedPrimaryKeyUniBidirectional {

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
	@DisplayName("유저, 주소 저장")
	public void save() {
		var user = User.of("user");
		var address = Address.of("address");
		entityTransaction.begin();
		entityManager.persist(user);
		entityManager.persist(address);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("유저, 주소 조회")
	public void find() {
		var user = entityManager.find(User.class, 1l);
		var address = entityManager.find(Address.class, 1l);
		log.info("user : {}", user);
		log.info("address : {}", address);
	}
}
