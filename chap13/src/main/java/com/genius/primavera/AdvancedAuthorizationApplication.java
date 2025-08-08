package com.genius.primavera;

import com.genius.primavera.application.storage.StorageService;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableMongoAuditing
@EnableReactiveMongoRepositories
@EnableConfigurationProperties(ThymeleafProperties.class)
public class AdvancedAuthorizationApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AdvancedAuthorizationApplication.class)
                .properties("spring.config.additional-location=classpath:/social.yml")
                .build()
                .run(args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}