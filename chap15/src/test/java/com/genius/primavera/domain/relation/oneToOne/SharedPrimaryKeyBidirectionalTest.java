package com.genius.primavera.domain.relation.oneToOne;

import com.genius.primavera.domain.relation.JpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("1:1 translated_text_2 translated_text_4 translated_text_2 translated_text_1 translated_text_3")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SharedPrimaryKeyBidirectionalTest extends JpaTestBase {

	@Test
	@Order(1)
	@DisplayName("translated_text_2, ISBN translated_text_2")
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
	@DisplayName("translated_text_2, ISBN inquiry")
	public void find() {
		var book = entityManager.find(Book.class, 1l);
		var isbn = entityManager.find(ISBN.class, 1l);

		log.info("book : {}", book);

		log.info("isbn : {}", isbn);
	}
}
