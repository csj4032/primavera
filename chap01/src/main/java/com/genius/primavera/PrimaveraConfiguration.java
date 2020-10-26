package com.genius.primavera;

import com.genius.primavera.application.IHelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

@Slf4j
@Configuration
public class PrimaveraConfiguration {

	@Bean
	public PrimaveraSpringBean annotationSpringBean() {
		return new PrimaveraSpringBean("annotationSpringBean");
	}

	@Bean("helloServiceRequest")
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public IHelloService helloServiceRequest() {
		return () -> Collections.EMPTY_LIST;
	}

	@Bean("helloServicePrototype")
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public IHelloService helloServicePrototype() {
		return () -> Collections.EMPTY_LIST;
	}
}