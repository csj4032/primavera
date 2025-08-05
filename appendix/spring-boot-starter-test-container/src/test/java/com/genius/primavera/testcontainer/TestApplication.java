package com.genius.primavera.testcontainer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TestContainers 테스트용 Spring Boot 애플리케이션
 * 
 * @SpringBootTest 어노테이션이 자동으로 이 Configuration을 찾아서 사용합니다.
 * 테스트에 필요한 최소한의 Spring Boot 설정만 포함합니다.
 */
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}