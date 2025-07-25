package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@Slf4j
@SpringBootApplication
public class OAuth2SocialLoginApplication {

	public static void main(String[] args) {
		// System property for better logging during startup
		System.setProperty("spring.output.ansi.enabled", "always");
		
		// Configure and run the application
		SpringApplication app = new SpringApplicationBuilder(OAuth2SocialLoginApplication.class)
				.bannerMode(Banner.Mode.OFF) // We have custom banner in listener
				.build();
		
		// Add shutdown hook for graceful shutdown logging
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.info("🛑 Primavera Application Shutting Down...");
			log.info("👋 Goodbye! Thank you for using Primavera Community Platform!");
		}));
		
		app.run(args);
	}
}