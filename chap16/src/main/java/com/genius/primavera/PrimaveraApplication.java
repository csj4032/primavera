package com.genius.primavera;

import com.genius.primavera.infrastructure.sentry.EnableSentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.ArrayList;

@Slf4j
@EnableSentry
@SpringBootApplication
public class PrimaveraApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PrimaveraApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.build()
				.run(args);
	}
}