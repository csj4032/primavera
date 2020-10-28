package com.genius.primavera.domain.relation.oneToOne;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

@Slf4j
@DisplayName("1:1 주 테이블에 외래 키 단방향")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImplementingForeignKeyBidirectional {

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
	@DisplayName("상품과 시리얼 저장")
	public void save() {
		var serial = Serial.of("serial1", Serial.Type.Type1);
		var product = Product.of("product1", serial);
		serial.setProduct(product);
		entityTransaction.begin();
		entityManager.persist(product);
		entityManager.persist(serial);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("상품과 시리얼 조회")
	public void find() {
		var product = entityManager.find(Product.class, 1l);
		var serial = entityManager.find(Serial.class, 1l);
		log.info("product : {}", product);
		log.info("serial : {}", serial);
	}
}
