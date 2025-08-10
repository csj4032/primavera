package com.genius.primavera.domain.relation.oneToOne;

import com.genius.primavera.domain.relation.JpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("1:1 translated_text_1 translated_text_4 translated_text_2 translated_text_1 translated_text_3")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImplementingForeignKeyBidirectionalTest extends JpaTestBase {

	@Test
	@Order(1)
	@DisplayName("translated_text_3 translated_text_3 translated_text_2")
	public void save() {
		var serial = Serial.of("serial1", Serial.Type.Type1);
		var product = Product.of("product1", serial);
		serial.setProduct(product);
		entityTransaction.begin();
		entityManager.persist(product);
		entityManager.persist(serial);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("translated_text_3 translated_text_3 inquiry")
	public void find() {
		var product = entityManager.find(Product.class, 1l);
		var serial = entityManager.find(Serial.class, 1l);
		log.info("product : {}", product);
		log.info("serial : {}", serial);
	}
}
