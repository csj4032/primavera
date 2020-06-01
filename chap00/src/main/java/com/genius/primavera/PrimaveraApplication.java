package com.genius.primavera;

import com.genius.primavera.application.WorldService;
import com.genius.primavera.interfaces.HelloController;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.support.GenericApplicationContext;

@SpringBootConfiguration
@EnableAutoConfiguration
public class PrimaveraApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PrimaveraApplication.class)
				.initializers((GenericApplicationContext applicationContext) -> {
					applicationContext.registerBean("world", WorldService.class, () -> () -> "World!!!");
					applicationContext.registerBean(HelloController.class, () -> new HelloController(() -> "Hello", applicationContext.getBean("world", WorldService.class)));
				})
				.build().run(args);
	}
}