package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ImportResource("classpath:configuration.xml")
public class ConfigurationDependencyApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ConfigurationDependencyApplication.class).build().run(args);
    }
}