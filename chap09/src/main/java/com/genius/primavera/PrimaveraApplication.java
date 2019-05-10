package com.genius.primavera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

@EnableOAuth2Client
@SpringBootApplication
public class PrimaveraApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrimaveraApplication.class, args);
	}
}