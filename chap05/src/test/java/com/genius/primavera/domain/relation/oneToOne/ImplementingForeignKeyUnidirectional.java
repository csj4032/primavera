package com.genius.primavera.domain.relation.oneToOne;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

@Slf4j
@DisplayName("1:1 주 테이블에 외래 키 단방향")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImplementingForeignKeyUnidirectional {

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
	@DisplayName("게시물, 내용 저장")
	public void save() {
		var content = Content.of("content1");
		var article = Article.of("subject1", content);
		entityTransaction.begin();
		entityManager.persist(article);
		entityManager.persist(content);
		entityTransaction.commit();
	}


	@Test
	@Order(2)
	@DisplayName("게시물, 내용 조회")
	public void find() {
		var article = entityManager.find(Article.class, 2l);
		var content = entityManager.find(Content.class, 1l);
		log.info("article : {}", article);
		log.info("content : {}", content);
	}
}
