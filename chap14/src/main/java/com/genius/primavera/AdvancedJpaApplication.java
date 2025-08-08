package com.genius.primavera;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ThymeleafProperties.class)
public class AdvancedJpaApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AdvancedJpaApplication.class)
                .properties("spring.config.additional-location=classpath:/social.yml")
                .build()
                .run(args);
    }
}