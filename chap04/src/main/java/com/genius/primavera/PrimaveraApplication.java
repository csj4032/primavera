package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan("com.genius.primavera.domain.mapper")
public class PrimaveraApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrimaveraApplication.class, args);
		log.debug("PrimaveraApplication Start Debug");
		log.info("PrimaveraApplication Start Info");
		log.warn("PrimaveraApplication Start Warn");
		log.error("PrimaveraApplication Start Error");
	}
}