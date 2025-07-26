package com.genius.primavera.autoconfigure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class LucyFilterAutoConfigurationTest {

	protected AnnotationConfigWebApplicationContext context;

	@Test
	public void lucyFilterAutoConfigurationCreated() throws Exception {
		this.context = new AnnotationConfigWebApplicationContext();
		this.context.register(LucyFilterAutoConfiguration.class);
		this.context.refresh();
		Assertions.assertNotNull(this.context.getBean(LucyFilterAutoConfiguration.class));
	}
}