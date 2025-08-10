package com.genius.primavera.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Slf4j
@Component
public class AnnotationClass {

	@PostConstruct
	public void init(){
		log.info("Annotation Bean creation should connection : init() calledshould");
	}

	@PreDestroy
	public void destroy(){
		log.info("Annotation Bean creation test : destroy calledshould");
	}
}