package com.genius.primavera.domain.hierarchy;

import com.genius.primavera.BaseHierarchyJpaTest;
import com.genius.primavera.domain.hierarchy.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("translated_text_2 translated_text_3 translated_text_2 translated_text_2")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SingleTableStrategyTest extends BaseHierarchyJpaTest {

	@Test
	@Order(1)
	@DisplayName("translated_text_3 translated_text_2")
	public void save() {
		var album = new Album("translated_text_2", 100, "album");
		entityTransaction.begin();
		entityManager.persist(album);
		entityTransaction.commit();

	}

	@Test
	@Order(2)
	@DisplayName("translated_text_3 inquiry")
	public void find() {
		var album = entityManager.find(Album.class, 1L);
		log.info("album : {}", album);
	}
}