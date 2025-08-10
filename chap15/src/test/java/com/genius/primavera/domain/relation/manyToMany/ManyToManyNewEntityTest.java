package com.genius.primavera.domain.relation.manyToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.time.Instant;

@Slf4j
@DisplayName("N:N connection test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToManyNewEntityTest extends ManyToManyTestBase {

	@Test
	@Order(1)
	@DisplayName("connection, connection test")
	public void save() {
		var buyer1 = Buyer.of("buyer1");
		var buyer2 = Buyer.of("buyer2");
		var seller1 = Seller.of("seller1");
		var seller2 = Seller.of("seller2");

		var contract1 = new Contract(null, buyer1, seller1, Instant.now());
		var contract2 = new Contract(null, buyer1, seller2, Instant.now());
		var contract3 = new Contract(null, buyer2, seller1, Instant.now());
		var contract4 = new Contract(null, buyer2, seller2, Instant.now());

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
	@DisplayName("connection, connection test inquiry")
	public void find() {
		Contract contract = entityManager.find(Contract.class, 1l);
		log.info("contract : {}", contract);
		log.info("contract buyer : {}", contract.getBuyer());
		log.info("contract seller : {}", contract.getSeller());
	}
}
