package com.genius.primavera.scope;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScopeRunner implements ApplicationRunner {

	@Autowired
	protected Singleton singleton;

	@Autowired
	private Prototype prototype;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void run(ApplicationArguments args) {
		log.info("Singleton : {}", singleton);
		log.info("Singleton : {}", singleton);
		log.info("");
		log.info("Prototype : {}", prototype);
		log.info("Prototype : {}", prototype);
		log.info("");
		log.info("Singleton : {}", applicationContext.getBean(Singleton.class));
		log.info("Singleton : {}", applicationContext.getBean(Singleton.class));
		log.info("");
		log.info("Singleton ProtoType : {}", applicationContext.getBean(Singleton.class).getPrototype());
		log.info("Singleton ProtoType : {}", applicationContext.getBean(Singleton.class).getPrototype());
		log.info("");
		log.info("Prototype : {}", applicationContext.getBean(Prototype.class));
		log.info("Prototype : {}", applicationContext.getBean(Prototype.class));
	}
}