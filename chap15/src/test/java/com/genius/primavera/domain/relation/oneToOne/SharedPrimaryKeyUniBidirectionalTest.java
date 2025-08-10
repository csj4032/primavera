package com.genius.primavera.domain.relation.oneToOne;

import com.genius.primavera.domain.relation.JpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("1:1 test file test should connection")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SharedPrimaryKeyUniBidirectionalTest extends JpaTestBase {

	@Test
	@Order(1)
	@DisplayName("test, test")
	public void save() {
		var member = Member.of("member");
		var address = Address.of("address");
		entityTransaction.begin();
		entityManager.persist(member);
		entityManager.persist(address);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("test, test inquiry")
	public void find() {
		var member = entityManager.find(Member.class, 1l);
		var address = entityManager.find(Address.class, 1l);
		log.info("member : {}", member);
		log.info("address : {}", address);
	}
}
