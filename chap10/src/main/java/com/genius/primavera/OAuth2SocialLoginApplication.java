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
        SpringApplication app = new SpringApplicationBuilder(OAuth2SocialLoginApplication.class)
                .bannerMode(Banner.Mode.CONSOLE)
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("🛑 Primavera Application Shutting Down...");
            log.info("👋 Goodbye! Thank you for using Primavera Community Platform!");
        }));
        app.run(args);
    }
}