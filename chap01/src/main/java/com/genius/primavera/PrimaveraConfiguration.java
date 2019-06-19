package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PrimaveraConfiguration {

	@Bean
	public PrimaveraSpringBean annotationSpringBean() {
		return new PrimaveraSpringBean("annotationSpringBean");
	}
}