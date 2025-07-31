package com.genius.primavera.testContainer;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 테스트용 Spring Boot 설정 클래스
 * TestContainer 스타터를 테스트하기 위한 최소한의 설정을 제공합니다.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.genius.primavera.testContainer")
public class TestConfiguration {
    // 테스트용 설정 - 별도 빈 설정 불필요
}