package com.genius.primavera;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PrimaveraApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PrimaveraApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .run();
    }
}