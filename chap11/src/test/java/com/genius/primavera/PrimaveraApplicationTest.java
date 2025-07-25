package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.genius.primavera.domain.model")
class PrimaveraTestApplication {
}

@Slf4j
@SpringBootTest(classes = PrimaveraTestApplication.class, properties = {
	"spring.main.web-application-type=none",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
	"spring.sql.init.mode=never",
	"spring.flyway.enabled=false",
	"spring.application.name=primavera-test"
})
@ActiveProfiles("test") 
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimaveraApplicationTest {

	@Autowired
	private ConfigurableApplicationContext configurableApplicationContext;

	@Test
	@Order(1)
	@DisplayName("ActiveProfile")
	public void activeProfileTest() {
		Assertions.assertArrayEquals(new String[]{"test"}, configurableApplicationContext.getEnvironment().getActiveProfiles());
	}

	@Test
	@Order(2)
	@DisplayName("ContainsProperty")
	public void containsPropertyTest() {
		Assertions.assertTrue(configurableApplicationContext.getEnvironment().containsProperty("spring.application.name"));
		Assertions.assertTrue(configurableApplicationContext.getEnvironment().containsProperty("spring.datasource.url"));
	}
}