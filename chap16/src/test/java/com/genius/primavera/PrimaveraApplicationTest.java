package com.genius.primavera;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimaveraApplicationTest {

	private static ConfigurableApplicationContext configurableApplicationContext;
	private static final String APPLICATION = "spring.config.location=classpath:/application-${spring.profiles.active:default}.yml,classpath:/social.yml";

	@BeforeAll
	public static void setUp() {
		configurableApplicationContext = new SpringApplicationBuilder(PrimaveraApplicationTest.class)
				.properties(APPLICATION)
				.web(WebApplicationType.NONE)
				.build().run();
	}

	@Test
	@Order(1)
	@DisplayName("ActiveProfile")
	public void activeProfileTest() {

		Assertions.assertArrayEquals(new String[]{}, configurableApplicationContext.getEnvironment().getActiveProfiles());
	}

	@Test
	@Order(2)
	@DisplayName("ContainsProperty")
	public void containsPropertyTest() {
		Assertions.assertTrue(configurableApplicationContext.getEnvironment().containsProperty("spring.application.name"));
		Assertions.assertTrue(configurableApplicationContext.getEnvironment().containsProperty("google.client.clientId"));
	}
}