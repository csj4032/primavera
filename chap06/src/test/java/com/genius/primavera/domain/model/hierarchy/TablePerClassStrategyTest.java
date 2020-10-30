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
 * 서브 타입을 구분해서 처리할 때 효과적
 * not null 제약조건을 사용
 * 단점
 * 여러 자식 테이블을 함께 조회할 때 성능이 느림
 * 자식 테이블을 통합해서 쿼리하기 어려움
 * 특징
 * 구분 컬럼을 사용하지 않음
 */
@Slf4j
@DisplayName("클래스마다 테이블 매핑")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TablePerClassStrategyTest {

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
	@DisplayName("과목 저장")
	public void save() {
		var canidae = new Canidae();
		canidae.setName("개과");
		canidae.setCanini("갈기늑대속");
		canidae.setRegDt(LocalDateTime.now());

		var scincidae = new Scincidae();
		scincidae.setName("고양이과");
		scincidae.setGenus("치타");
		scincidae.setRegDt(LocalDateTime.now());

		var felidae = new Felidae();
		felidae.setName("도마뱀");
		felidae.setSystem("목도리");
		felidae.setRegDt(LocalDateTime.now());

		entityTransaction.begin();
		entityManager.persist(canidae);
		entityManager.persist(scincidae);
		entityTransaction.commit();

		entityTransaction.begin();
		entityManager.persist(felidae);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("과목 조회")
	public void find() {
		var canidae = entityManager.find(Canidae.class, 1L);
		var scincidae = entityManager.find(Scincidae.class, 2L);
		var felidae = entityManager.find(Felidae.class, 3L);
		log.info("canidae : {}", canidae);
		log.info("scincidae : {}", scincidae);
		log.info("felidae : {}", felidae);
	}
}