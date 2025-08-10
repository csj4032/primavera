package com.genius.primavera;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class HierarchicalCommentApplication {

    private static final String APPLICATION = "spring.config.location=classpath:/application-${spring.profiles.active:default}.yml,classpath:/social.yml";

    public static void main(String[] args) {
        new SpringApplicationBuilder(HierarchicalCommentApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .properties(APPLICATION)
                .build()
                .run(args);
    }
}