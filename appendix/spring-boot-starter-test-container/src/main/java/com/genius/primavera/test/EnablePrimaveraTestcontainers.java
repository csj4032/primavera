package com.genius.primavera.test;

import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Primavera TestContainers 활성화 애노테이션
 * 
 * 이 애노테이션을 테스트 클래스에 추가하면 지정된 컨테이너 타입들이 자동으로 시작됩니다.
 * ApplicationContextInitializer를 통해 Spring 컨텍스트 초기화 시점에 모든 설정이 완료됩니다.
 * 
 * 사용 예시:
 * 
 * 1. 기본 MariaDB만 사용:
 * @SpringBootTest
 * @EnablePrimaveraTestcontainers
 * class MyTest {
 *     @Autowired private DataSource dataSource;
 * }
 * 
 * 2. 특정 컨테이너들 사용:
 * @SpringBootTest
 * @EnablePrimaveraTestcontainers({ContainerType.MARIADB, ContainerType.REDIS})
 * class MyTest {
 *     @Autowired private DataSource dataSource;
 *     @Autowired private RedisTemplate redisTemplate;
 * }
 * 
 * 3. 전체 스택 사용:
 * @SpringBootTest
 * @EnablePrimaveraTestcontainers({ContainerType.MARIADB, ContainerType.REDIS, ContainerType.KAFKA})
 * class MyIntegrationTest {
 *     // 모든 인프라 컴포넌트 사용 가능
 * }
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(initializers = PrimaveraTestcontainersContextInitializer.class)
public @interface EnablePrimaveraTestcontainers {
    
    /**
     * 시작할 컨테이너 타입들을 지정합니다.
     * 지정하지 않으면 기본적으로 MARIADB 컨테이너만 시작됩니다.
     * 
     * @return 시작할 컨테이너 타입 배열
     */
    ContainerType[] value() default {ContainerType.MARIADB};
    
    /**
     * 컨테이너 시작 타임아웃 (초)
     * 기본값: 60초
     * 
     * @return 타임아웃 시간 (초)
     */
    int startupTimeout() default 60;
    
    /**
     * 테스트 종료 후 컨테이너를 자동으로 정지할지 여부
     * 기본값: true (자동 정지)
     * 
     * @return 자동 정지 여부
     */
    boolean autoStop() default true;
    
    /**
     * 컨테이너 재사용 여부
     * true로 설정하면 동일한 설정의 컨테이너를 여러 테스트에서 재사용합니다.
     * 기본값: true (성능 향상을 위해 재사용)
     * 
     * @return 재사용 여부
     */
    boolean reuse() default true;
}