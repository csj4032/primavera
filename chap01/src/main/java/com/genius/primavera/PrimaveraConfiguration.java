package com.genius.primavera;

import com.genius.primavera.application.ScopeService;
import com.genius.primavera.application.ScopeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;

@Slf4j
@Configuration
public class PrimaveraConfiguration {

	@Bean
	public PrimaveraSpringBean annotationSpringBean() {
		return new PrimaveraSpringBean("annotationSpringBean");
	}

	@RequestScope
	@Bean("scopeServiceRequest")
	//@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ScopeService helloServiceRequest() {
		return new ScopeServiceImpl();
	}

	@Bean("scopeServiceRequestAnonymous")
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ScopeService helloServiceRequestAnonymous() {
		return () -> Collections.EMPTY_LIST;
	}

	@Bean("scopeServiceProtoType")
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public ScopeService helloServicePrototype() {
		return new ScopeServiceImpl();
	}
}