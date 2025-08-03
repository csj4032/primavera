package com.genius.primavera;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import java.util.TimeZone;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BoardSystemApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(BoardSystemApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.build()
				.run(args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}