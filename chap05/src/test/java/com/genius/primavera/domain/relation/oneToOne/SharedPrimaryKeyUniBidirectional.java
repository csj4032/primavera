package com.genius.primavera.domain.relation.oneToOne;

import com.genius.primavera.domain.relation.JpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

/**
 * 객체의 참조는 User 가지고 실제 테이블의 외래키는 Address 에 존재 하도록 매핑 할 수 없음
 */
@Slf4j
@DisplayName("1:1 대상 테이블에 외래 키 단방향")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SharedPrimaryKeyUniBidirectional extends JpaTestBase {

	@Test
	@Order(1)
	@DisplayName("유저, 주소 저장")
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
	@DisplayName("유저, 주소 조회")
	public void find() {
		var member = entityManager.find(Member.class, 1l);
		var address = entityManager.find(Address.class, 1l);
		log.info("member : {}", member);
		log.info("address : {}", address);
	}
}
