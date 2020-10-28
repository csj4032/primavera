package com.genius.primavera.domain.relation.manyToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

@Slf4j
@DisplayName("N:N 새로운 복합키 도출")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToManyCompositeKey {

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
	@DisplayName("발신자, 수신사 발신내용 저장")
	public void save() {
		var sender1 = Sender.of("Sender1");
		var sender2 = Sender.of("Sender2");
		var recipient1 = Recipient.of("Recipient1");
		var recipient2 = Recipient.of("Recipient2");

		var letter1 = new Letter(sender1, recipient1, "Message 1-1", Letter.Type.PAYMENT);
		var letter2 = new Letter(sender1, recipient2, "Message 1-2", Letter.Type.PAYMENT);
		var letter3 = new Letter(sender2, recipient1, "Message 2-1", Letter.Type.DEFERRED);
		var letter4 = new Letter(sender2, recipient2, "Message 2-2", Letter.Type.DEFERRED);

		entityTransaction.begin();
		entityManager.persist(sender1);
		entityManager.persist(sender2);
		entityManager.persist(recipient1);
		entityManager.persist(recipient2);
		entityManager.persist(letter1);
		entityManager.persist(letter2);
		entityManager.persist(letter3);
		entityManager.persist(letter4);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("발신자, 수신사 발신내용 조회")
	public void find() {
		LetterId letterId = new LetterId(1L, 1L);
		Letter letter = entityManager.find(Letter.class, letterId);
		log.info("letter : {}", letter);
		log.info("sender : {}", letter.getSender());
		log.info("recipient : {}", letter.getRecipient());
	}
}
