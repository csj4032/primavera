package com.genius.primavera.domain.relation.manyToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("N:N connection test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToManyCompositeKeyTest extends ManyToManyTestBase {

	@Test
	@Order(1)
	@DisplayName("connection, connection file test")
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
	@DisplayName("connection, connection file inquiry")
	public void find() {
		LetterId letterId = new LetterId(1L, 1L);
		Letter letter = entityManager.find(Letter.class, letterId);
		log.info("letter : {}", letter);
		log.info("sender : {}", letter.getSender());
		log.info("recipient : {}", letter.getRecipient());
	}
}
