package com.genius.primavera.domain.relation.oneToMany;

import com.genius.primavera.domain.relation.JpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@DisplayName("1:N connection connection")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OneToManyBidirectionalTest extends JpaTestBase {

	@Test
	@Order(1)
	@DisplayName("test, connection test")
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
	@DisplayName("test, connection inquiry")
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
	@DisplayName("test connection test")
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
	@DisplayName("test connection should test modification should test inquiry")
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
	@DisplayName("connection test modification inquiry")
	public void updateCustomByContact() {
		var contact1 = entityManager.find(Contact.class, 1l);
		var customer2 = Customer.of("custom2");
		
		entityTransaction.begin();
		entityManager.persist(customer2);
		contact1.setCustomer(customer2);
		entityManager.persist(contact1);
		entityTransaction.commit();
	}
}