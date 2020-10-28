package com.genius.primavera.domain.relation.oneToOne;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

@Slf4j
@DisplayName("1:1 대상 테이블에 외래 키 양방향")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SharedPrimaryKeyBidirectional {

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
	@DisplayName("서적, ISBN 저장")
	public void save() {
		var book = Book.of("book1");
		var isbn = ISBN.of("isbn1", book);
		book.setIsbn(isbn);
		entityTransaction.begin();
		entityManager.persist(isbn);
		entityManager.persist(book);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("서적, ISBN 조회")
	public void find() {
		var book = entityManager.find(Book.class, 1l);
		var isbn = entityManager.find(ISBN.class, 1l);
		//Lazy X
		log.info("book : {}", book);
		//Lazy O
		log.info("isbn : {}", isbn);
	}
}
