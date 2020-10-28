package com.genius.primavera.domain.relation.oneToMany;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * 일대다 단방향 매핑 반대편에 다대일 단뱡향 매핑을 읽기 전용으로 추가해서 일대다 양방향 처럼 보임
 * 다대일 양방향 권장
 */
@Slf4j
@DisplayName("1:N 고객와 연락처 양방향")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OneToManyBidirectional {

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
	@DisplayName("고객, 연락처 저장")
	public void save() {
		var customer = Customer.of("customer1");
		var contact1 = new Contact(null, Contact.Type.EMAIL, "csj4032", customer);
		var contact2 = new Contact(null, Contact.Type.EMAIL, "csj4032", customer);
		customer.getContacts().add(contact1);
		customer.getContacts().add(contact2);

		entityTransaction.begin();
		entityManager.persist(contact1);
		entityManager.persist(contact2);
		entityManager.persist(customer);
		entityTransaction.commit();
	}

	@Test
	@Order(2)
	@DisplayName("고객, 연락처 조회")
	public void find() {
		var contact1 = entityManager.find(Contact.class, 1l);
		var contact2 = entityManager.find(Contact.class, 2l);
		log.info("contact1 : {}", contact1);
		log.info("contact2 : {}", contact2);
		var customer = entityManager.find(Customer.class, 1l);
		log.info("customer : {}", customer);
	}

	@Test
	@Order(3)
	@DisplayName("고객 연락처 추가")
	public void update() {
		var customer = entityManager.find(Customer.class, 1l);
		var contact3 = new Contact(null, Contact.Type.PHONE, "csj4032", customer);
		customer.getContacts().add(contact3);
		entityTransaction.begin();
		entityManager.persist(contact3);
		entityManager.persist(customer);
		entityTransaction.commit();
	}

	@Test
	@Order(4)
	@DisplayName("고객 연락처 중 일부 수정 후 고객 조회")
	public void updateContact() {
		var contact1 = entityManager.find(Contact.class, 1l);
		contact1.setType(Contact.Type.PHONE);
		entityTransaction.begin();
		entityManager.persist(contact1);
		entityTransaction.commit();
		var customer = entityManager.find(Customer.class, 1l);
		log.info("customer : {}", customer);
	}

	@Test
	@Order(5)
	@DisplayName("연락처의 고객정보 수정 조회")
	public void updateCustomByContact() {
		var contact1 = entityManager.find(Contact.class, 1l);
		var customer2 = Customer.of("custom2");
		contact1.setCustomer(customer2);
		entityTransaction.begin();
		entityManager.persist(contact1);
		entityTransaction.commit();
	}
}