package com.genius.primavera;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpringBootStarterApplication.class)
public class BeanRegistrationDebugTest {

    private static final Logger log = LoggerFactory.getLogger(BeanRegistrationDebugTest.class);

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("all created successfully Bean test")
    void listAllBeans() {
        String[] beanNames = context.getBeanDefinitionNames();
        log.info("=== created successfully all Bean test ===");
        Arrays.stream(beanNames)
                .filter(name -> name.contains("world") || name.contains("hello") || name.contains("greeting"))
                .sorted()
                .forEach(name -> log.info("Bean: {}", name));
        
        log.info("=== test Bean test: {} ===", beanNames.length);

        try {
            Object worldController = context.getBean("worldController");
            log.info("worldController Bean test: {}", worldController.getClass().getName());
            assertThat(worldController).isNotNull();
        } catch (Exception e) {
            log.error("worldController Bean test should test: {}", e.getMessage());
        }

        try {
            Object helloService = context.getBean("helloService");
            log.info("helloService Bean test: {}", helloService.getClass().getName());
        } catch (Exception e) {
            log.error("helloService Bean test should test: {}", e.getMessage());
        }

        Arrays.stream(beanNames)
                .filter(name -> name.toLowerCase().contains("controller"))
                .forEach(name -> log.info("Controller Bean: {}", name));

        Arrays.stream(beanNames)
                .filter(name -> name.toLowerCase().contains("service"))
                .forEach(name -> log.info("Service Bean: {}", name));
    }
}