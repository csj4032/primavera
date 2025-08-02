package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

/**
 * Spring Boot 설정과 의존성 주입 학습용 애플리케이션
 * 
 * 이 애플리케이션은 Spring의 다양한 설정 방식과 Bean 등록 방법을 학습하기 위한 예제입니다.
 * 
 * 주요 학습 내용:
 * - Bean Scope (Singleton vs Prototype) 동작 확인
 * - 다양한 Bean 등록 방식 (어노테이션, XML, Java Config)
 * - Bean 생명주기 콜백 (@PostConstruct, InitializingBean)
 * - 의존성 주입 패턴 (Constructor, Setter, Field Injection)
 * - AOP(Aspect-Oriented Programming) 활성화
 * - Configuration Properties 타입 안전한 설정 바인딩
 * - XML 설정과 Java 설정의 혼합 사용
 * 
 * 실행 시 확인 가능한 내용:
 * - Singleton Bean: 동일한 인스턴스 재사용
 * - Prototype Bean: 매번 새로운 인스턴스 생성
 * - Bean 초기화 순서와 생명주기 로그
 * 
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 01-05: Spring Boot 기초)
 * =============================================================================
 * 
 * 1️⃣ 인프라 시작:
 *    cd infrastructure
 *    docker-compose -f docker-compose.basic.yml up -d
 * 
 * 2️⃣ 애플리케이션 실행:
 *    ./gradlew :chap02:bootRun -Dspring.profiles.active=local
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308 (primavera/primavera)
 * 
 * =============================================================================
 * 
 * @author Genius
 */
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