package com.genius.primavera.domain.model.hierarchy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

/**
 * 장점
 * 조인이 필요 없으므로 일반적으로 조회 성능이 빠름
 * 조회 쿼리가 단순
 * 단점
 * 자식 엔티티가 매핑한 컬럼은 모두 null 허용
 * 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있음, 성능 이슈
 * 특징
 * 구분 컬럼을 꼭 사용
 * @DiscriminatorValue를 지정하지 않으면 기본으로 엔티티 이름을 이용
 */
@Slf4j
@DisplayName("단일 테이블 전략 매핑")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SingleTableStrategyTest {

	private static EntityManagerFactory entityManagerFactory;
	private static EntityManager entityManager;
	private static EntityTransaction entityTransaction;

	@BeforeAll
	public static void setUp() {
		entityManagerFactory = Persistence.createEntityManagerFactory("advance");
		entityManager = entityManagerFactory.createEntityManager();
		entityTransaction = entityManager.getTransaction();
	}

	@Test
	@Order(1)
	@DisplayName("아이템 저장")
	public void save() {
		var album = new Album("앨범", 100, "album");
		entityTransaction.begin();
		entityManager.persist(album);
		entityTransaction.commit();

	}

	@Test
	@Order(2)
	@DisplayName("아이템 조회")
	public void find() {
		var album = entityManager.find(Album.class, 1L);
		log.info("album : {}", album);
	}
}