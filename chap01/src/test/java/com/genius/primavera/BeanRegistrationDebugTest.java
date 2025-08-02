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
    @DisplayName("모든 등록된 Bean 이름 출력")
    void listAllBeans() {
        String[] beanNames = context.getBeanDefinitionNames();
        log.info("=== 등록된 모든 Bean 이름 ===");
        Arrays.stream(beanNames)
                .filter(name -> name.contains("world") || name.contains("hello") || name.contains("greeting"))
                .sorted()
                .forEach(name -> log.info("Bean: {}", name));
        
        log.info("=== 전체 Bean 개수: {} ===", beanNames.length);
        
        // WorldController 타입으로 Bean 찾기 시도
        try {
            Object worldController = context.getBean("worldController");
            log.info("worldController Bean 찾음: {}", worldController.getClass().getName());
            assertThat(worldController).isNotNull();
        } catch (Exception e) {
            log.error("worldController Bean 찾을 수 없음: {}", e.getMessage());
        }
        
        // HelloService 타입으로 Bean 찾기 시도
        try {
            Object helloService = context.getBean("helloService");
            log.info("helloService Bean 찾음: {}", helloService.getClass().getName());
        } catch (Exception e) {
            log.error("helloService Bean 찾을 수 없음: {}", e.getMessage());
        }
        
        // 모든 Controller 관련 Bean 출력
        Arrays.stream(beanNames)
                .filter(name -> name.toLowerCase().contains("controller"))
                .forEach(name -> log.info("Controller Bean: {}", name));
                
        // 모든 Service 관련 Bean 출력
        Arrays.stream(beanNames)
                .filter(name -> name.toLowerCase().contains("service"))
                .forEach(name -> log.info("Service Bean: {}", name));
    }
}