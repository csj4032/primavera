package com.genius.primavera;

import com.genius.primavera.application.HelloAspect;
import com.genius.primavera.application.SpringBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PrimaveraConfiguration {

	@Bean
	public SpringBean annotationSpringBean() {
		return new SpringBean("annotationSpringBean");
	}

	@Bean
	public HelloAspect helloAspect() {
		return new HelloAspect();
	}
}