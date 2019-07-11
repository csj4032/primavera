package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class PrimaveraApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrimaveraApplication.class, args);
	}

	@Autowired
	private GeniusService geniusService;

	@Bean
	ApplicationRunner applicationRunner() { return (args) -> geniusService.doSomething(); }
}