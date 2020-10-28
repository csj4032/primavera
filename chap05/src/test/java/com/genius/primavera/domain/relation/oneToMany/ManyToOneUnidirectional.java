package com.genius.primavera.domain.relation.oneToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

@Slf4j
@DisplayName("N:1 플레이어와 팀 단방향  : 팀에는 회원을 참조하는 필드가 없음")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManyToOneUnidirectional {

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
	@DisplayName("팀, 선수 저장")
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
	@DisplayName("팀, 선수 조회")
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