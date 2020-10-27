package com.genius.primavera.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Component
public class AnnotationClass {

	@PostConstruct
	public void init(){
		log.info("Annotation Bean 생성 및 초기화 : init() 호출됨");
	}

	@PreDestroy
	public void destroy(){
		log.info("Annotation Bean 생성 소멸 : destroy 호출됨");
	}
}