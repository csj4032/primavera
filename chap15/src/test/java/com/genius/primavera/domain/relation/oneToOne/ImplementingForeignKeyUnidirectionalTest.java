package com.genius.primavera.domain.relation.oneToOne;

import com.genius.primavera.domain.relation.JpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("1:1 should file test should connection")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImplementingForeignKeyUnidirectionalTest extends JpaTestBase {

	@Test
	@Order(1)
	@DisplayName("connection, test")
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
	@DisplayName("connection, test inquiry")
	public void find() {
		var article = entityManager.find(Article.class, 2l);
		var content = entityManager.find(Content.class, 1l);
		log.info("article : {}", article);
		log.info("content : {}", content);
	}
}
