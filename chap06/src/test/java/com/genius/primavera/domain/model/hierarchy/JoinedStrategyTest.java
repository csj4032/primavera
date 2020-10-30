package com.genius.primavera.domain.model.hierarchy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

/**
 * 장점
 * 테이블 정규화
 * 외래 키 참조 무결성 제약조건을 활용
 * 저장공간을 효율적으로 사용
 * 단점
 * 조회할 때 조인이 많이 사용 성능 저하 우려
 * 조회 쿼리가 복잡
 * 데이터를 등록할 INSERT SQL 두번 실행용
 * 특징
 * JPA 표준 명세는 구분 컬럼을 사용하도록 함
 */
@Slf4j
@DisplayName("조인 전략 매핑")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JoinedStrategyTest {

	private static EntityManagerFactory entityManagerFactory;
	private static EntityManager entityManager;
	private static EntityTransaction entityTransaction;

	@BeforeAll
	public static void setUp() {
		entityManagerFactory = Persistence.createEntityManagerFactory("advance");
		entityManager = entityManagerFactory.createEntityManager();
		entityTransaction = entityManager.getTransaction();
	}

	@Test
	@Order(1)
	@DisplayName("연락처 저장")
	public void save() {
		var address = new Address();
		address.setCountry("country");
		address.setCity("city");
		address.setStreet("street");
		address.setZipCode("zipCode");
		address.setRegDt(LocalDateTime.now());

		var email = new Email();
		email.setSign("genis");
		email.setDomain("gmail.com");
		email.setRegDt(LocalDateTime.now());

		var mobile = new Mobile();
		mobile.setProvider("010");
		mobile.setNumber("00000000");
		mobile.setRegDt(LocalDateTime.now());

		entityTransaction.begin();
		entityManager.persist(address);
		entityManager.persist(email);
		entityManager.persist(mobile);
		entityTransaction.commit();

	}

	@Test
	@Order(2)
	@DisplayName("연락처 조회")
	public void find() {
		var contact = entityManager.find(Address.class, 1L);
		log.info("contact : {}", contact);
	}
}