package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@SpringBootApplication
// Application.yml 우선순위
//@EnableAspectJAutoProxy(proxyTargetClass = true)
public class PrimaveraApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PrimaveraApplication.class).build().run(args);
	}

	@Autowired
	private GeniusService geniusService;

	@Bean
	ApplicationRunner applicationRunner() {
		return (args) -> geniusService.doSomething();
	}
}