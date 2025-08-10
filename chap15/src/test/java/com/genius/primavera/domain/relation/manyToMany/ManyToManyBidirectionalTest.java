package com.genius.primavera.domain.relation.manyToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("N:N translated_text_4 translated_text_3 translated_text_3")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToManyBidirectionalTest extends ManyToManyTestBase {

	@Test
	@Order(1)
    @Disabled
	@DisplayName("translated_text_3, translated_text_3 translated_text_2")
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
    @Disabled
	@DisplayName("translated_text_3, translated_text_3 inquiry")
	public void find() {
		var origin = entityManager.find(Origin.class, 1L);
		var destination = entityManager.find(Destination.class, 1L);
		log.info("origin : {}", origin);
		log.info("destination : {}", destination);
	}
}
