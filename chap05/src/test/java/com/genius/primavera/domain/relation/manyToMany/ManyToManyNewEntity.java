package com.genius.primavera.domain.relation.manyToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

@Slf4j
@DisplayName("N:N 새로운 엔티티 도출")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToManyNewEntity {

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
	@DisplayName("판매자, 구매자 계약 저장")
	public void save() {
		var buyer1 = Buyer.of("buyer1");
		var buyer2 = Buyer.of("buyer2");
		var seller1 = Seller.of("seller1");
		var seller2 = Seller.of("seller2");

		var contract1 = new Contract(null, buyer1, seller1, LocalDateTime.now());
		var contract2 = new Contract(null, buyer1, seller2, LocalDateTime.now());
		var contract3 = new Contract(null, buyer2, seller1, LocalDateTime.now());
		var contract4 = new Contract(null, buyer2, seller2, LocalDateTime.now());

		entityTransaction.begin();
		entityManager.persist(buyer1);
		entityManager.persist(buyer2);
		entityManager.persist(seller1);
		entityManager.persist(seller2);
		entityManager.persist(contract1);
		entityManager.persist(contract2);
		entityManager.persist(contract3);
		entityManager.persist(contract4);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("판매자, 구매자 계약 조회")
	public void find() {
		Contract contract = entityManager.find(Contract.class, 1l);
		log.info("contract : {}", contract);
		log.info("contract buyer : {}", contract.getBuyer());
		log.info("contract seller : {}", contract.getSeller());
	}
}
