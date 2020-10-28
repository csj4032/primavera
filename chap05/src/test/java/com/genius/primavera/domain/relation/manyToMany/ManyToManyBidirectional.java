package com.genius.primavera.domain.relation.manyToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

@Slf4j
@DisplayName("N:N 출발지과 도착지 양방향")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToManyBidirectional {

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
	@DisplayName("출발지, 도착지 저장")
	public void save() {
		var destination1 = Destination.of("Destination1");
		var destination2 = Destination.of("Destination2");
		var origin1 = Origin.of("Origin1");
		var origin2 = Origin.of("Origin2");

		origin1.getDestinations().add(destination1);
		origin1.getDestinations().add(destination2);
		origin2.getDestinations().add(destination1);
		origin2.getDestinations().add(destination2);

		destination1.getOrigins().add(origin1);
		destination1.getOrigins().add(origin2);
		destination2.getOrigins().add(origin1);
		destination2.getOrigins().add(origin2);

		entityTransaction.begin();
		entityManager.persist(destination1);
		entityManager.persist(destination2);
		entityManager.persist(origin1);
		entityManager.persist(origin2);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("출발지, 도착지 조회")
	public void find() {
		var origin = entityManager.find(Origin.class, 1l);
		var destination = entityManager.find(Destination.class, 1l);
		log.info("origin : {}", origin);
		log.info("destination : {}", destination);
	}
}
