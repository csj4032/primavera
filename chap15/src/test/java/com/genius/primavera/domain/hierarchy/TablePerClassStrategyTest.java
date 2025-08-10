package com.genius.primavera.domain.hierarchy;

import com.genius.primavera.BaseHierarchyJpaTest;
import com.genius.primavera.domain.hierarchy.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.time.Instant;

@Slf4j
@DisplayName("Endpoint connection test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TablePerClassStrategyTest extends BaseHierarchyJpaTest {

	@Test
	@Order(1)
	@DisplayName("test")
	public void save() {
		var canidae = new Canidae();
		canidae.setName("test");
		canidae.setCanini("Endpoint");
		canidae.setCreatedAt(Instant.now());

		var scincidae = new Scincidae();
		scincidae.setName("file");
		scincidae.setGenus("test");
		scincidae.setCreatedAt(Instant.now());

		var felidae = new Felidae();
		felidae.setName("connection");
		felidae.setSystem("connection");
		felidae.setCreatedAt(Instant.now());

		entityTransaction.begin();
		entityManager.persist(canidae);
		entityManager.persist(scincidae);
		entityTransaction.commit();

		entityTransaction.begin();
		entityManager.persist(felidae);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("test inquiry")
	public void find() {
		var canidae = entityManager.find(Canidae.class, 1L);
		var scincidae = entityManager.find(Scincidae.class, 2L);
		var felidae = entityManager.find(Felidae.class, 3L);
		log.info("canidae : {}", canidae);
		log.info("scincidae : {}", scincidae);
		log.info("felidae : {}", felidae);
	}
}