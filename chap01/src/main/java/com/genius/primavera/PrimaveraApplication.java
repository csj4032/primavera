package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;

@Slf4j
@SpringBootApplication
//@EnableAspectJAutoProxy(proxyTargetClass = true)
@ImportResource("classpath:configuration.xml")
public class PrimaveraApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PrimaveraApplication.class).build().run(args);
	}
}