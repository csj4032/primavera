package com.genius.primavera.domain.hierarchy;

import com.genius.primavera.BaseHierarchyJpaTest;
import com.genius.primavera.domain.hierarchy.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("test connection test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SingleTableStrategyTest extends BaseHierarchyJpaTest {

	@Test
	@Order(1)
	@DisplayName("connection test")
	public void save() {
		var album = new Album("test", 100, "album");
		entityTransaction.begin();
		entityManager.persist(album);
		entityTransaction.commit();

	}

	@Test
	@Order(2)
	@DisplayName("connection inquiry")
	public void find() {
		var album = entityManager.find(Album.class, 1L);
		log.info("album : {}", album);
	}
}