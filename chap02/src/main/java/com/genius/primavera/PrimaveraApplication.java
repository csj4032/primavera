package com.genius.primavera;

import com.genius.primavera.infrastructure.interception.PrimaveraInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Slf4j
@SpringBootApplication
public class PrimaveraApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PrimaveraApplication.class)
				.initializers(applicationContext -> {
					for (int i = 0; i < applicationContext.getBeanDefinitionNames().length; i++) {
						System.out.println("DDD " + applicationContext.getBeanDefinitionNames()[i]);
					}
				})
				.lazyInitialization(true)
				.build()
				.run();
	}

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReadyEvent(ApplicationReadyEvent applicationReadyEvent) {
		PrimaveraInterceptor primaveraInterceptor = (PrimaveraInterceptor) applicationReadyEvent.getApplicationContext().getBean("primaveraInterceptor");
		InterceptorRegistry registry = new InterceptorRegistry();
		registry.addInterceptor(primaveraInterceptor);
		WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter webMvcAutoConfigurationAdapter = (WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter) applicationReadyEvent.getApplicationContext()
				.getBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class);
		webMvcAutoConfigurationAdapter.addInterceptors(registry);
	}
}
