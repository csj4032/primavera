package com.genius.primavera.domain.relation.manyToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

@Slf4j
@DisplayName("N:N 발행과 구독 단방향  : 발행쪽에서 구독을 참조함")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToManyUnidirectional {

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
	@DisplayName("발행, 구독 저장")
	public void save() {
		var subscriber1 = new Subscriber(null, "Subscriber1");
		var subscriber2 = new Subscriber(null, "Subscriber2");
		var publisher = Publisher.of("Publisher1");
		publisher.getSubscribers().add(subscriber1);
		publisher.getSubscribers().add(subscriber2);
		entityTransaction.begin();
		entityManager.persist(publisher);
		entityManager.persist(subscriber1);
		entityManager.persist(subscriber2);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("발행, 구독 조회")
	public void find() {
		Publisher publisher = entityManager.find(Publisher.class, 1l);
		List<Subscriber> subscribers = publisher.getSubscribers();
		log.info("publisher : {}", publisher);
		for (Subscriber subscriber : subscribers) {
			log.info("subscriber : {}", subscriber);
		}
	}
}
