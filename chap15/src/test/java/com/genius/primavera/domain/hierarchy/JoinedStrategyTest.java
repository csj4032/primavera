package com.genius.primavera.domain.hierarchy;

import com.genius.primavera.BaseHierarchyJpaTest;
import com.genius.primavera.domain.hierarchy.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.time.Instant;

@Slf4j
@DisplayName("test test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JoinedStrategyTest extends BaseHierarchyJpaTest {

    @Test
    @Order(1)
    @DisplayName("connection test")
    public void save() {
        var address = new Address();
        address.setCountry("country");
        address.setCity("city");
        address.setStreet("street");
        address.setZipCode("zipCode");
        address.setCreatedAt(Instant.now());

        var email = new Email();
        email.setSign("genius");
        email.setDomain("gmail.com");
        email.setCreatedAt(Instant.now());

        var mobile = new Mobile();
        mobile.setProvider("010");
        mobile.setNumber("00000000");
        mobile.setCreatedAt(Instant.now());

        entityTransaction.begin();
        entityManager.persist(address);
        entityManager.persist(email);
        entityManager.persist(mobile);
        entityTransaction.commit();

    }

    @Test
    @Order(2)
    @DisplayName("connection inquiry")
    public void find() {
        var contact = entityManager.find(Address.class, 1L);
        log.info("contact : {}", contact);
    }
}