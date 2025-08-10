package com.genius.primavera.domain.relation.manyToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.List;

@Slf4j
@DisplayName("N:N translated_text_3 translated_text_2 translated_text_3  : translated_text_5 translated_text_2 translated_text_3")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToManyUnidirectionalTest extends ManyToManyTestBase {

	@Test
	@Order(1)
	@DisplayName("translated_text_2, translated_text_2 translated_text_2")
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
	@DisplayName("translated_text_2, translated_text_2 inquiry")
	public void find() {
		Publisher publisher = entityManager.find(Publisher.class, 1l);
		List<Subscriber> subscribers = publisher.getSubscribers();
		log.info("publisher : {}", publisher);
		for (Subscriber subscriber : subscribers) {
			log.info("subscriber : {}", subscriber);
		}
	}
}
