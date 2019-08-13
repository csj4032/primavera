package com.genius.primavera;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ThymeleafProperties.class)
public class PrimaveraApplication {

	private static final String APPLICATION = "spring.config.location=classpath:/application-${spring.profiles.active:default}.yml,classpath:/social.yml";

	public static void main(String[] args) {
		new SpringApplicationBuilder(PrimaveraApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.properties(APPLICATION)
				.build()
				.run(args);
	}
}