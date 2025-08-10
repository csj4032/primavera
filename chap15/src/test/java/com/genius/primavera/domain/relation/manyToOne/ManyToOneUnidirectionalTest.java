package com.genius.primavera.domain.relation.manyToOne;

import com.genius.primavera.domain.relation.JpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("N:1 translated_text_5 translated_text_1 translated_text_3  : translated_text_1 translated_text_5 translated_text_4 translated_text_3 translated_text_2")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToOneUnidirectionalTest extends JpaTestBase {

	@Test
	@Order(1)
	@DisplayName("translated_text_1, translated_text_2 translated_text_2")
	public void save() {
		var team = Team.of("Team1");
		var player1 = new Player(null, "Play1", team);
		var player2 = new Player(null, "Play2", team);
		var player3 = new Player(null, "Play3", null);
		entityTransaction.begin();
		entityManager.persist(player1);
		entityManager.persist(player2);
		entityManager.persist(player3);
		entityManager.persist(team);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("translated_text_1, translated_text_2 inquiry")
	public void find() {
		var play1 = entityManager.find(Player.class, 1l);
		var play2 = entityManager.find(Player.class, 2l);
		var play3 = entityManager.find(Player.class, 3l);
		log.info("play1 : {}", play1);
		log.info("play2 : {}", play2);
		log.info("play3 : {}", play3);
		var team = entityManager.find(Team.class, 1l);
		log.info("team1 : {}", team);
	}
}