package com.genius.primavera.testContainer;

import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Primavera TestContainers 활성화 애노테이션
 * <p>
 * 이 애노테이션을 테스트 클래스에 추가하면 지정된 컨테이너 타입들이 자동으로 시작됩니다.
 * ApplicationContextInitializer를 통해 Spring 컨텍스트 초기화 시점에 모든 설정이 완료됩니다.
 * <p>
 * 사용 예시:
 * <p>
 * 1. 기본 MariaDB만 사용:
 *
 * @SpringBootTest
 * @EnablePrimaveraTestcontainers class MyTest {
 * @Autowired private DataSource dataSource;
 * }
 * <p>
 * 2. 특정 컨테이너들 사용:
 * @SpringBootTest
 * @EnablePrimaveraTestcontainers({ContainerType.MARIADB, ContainerType.REDIS})
 * class MyTest {
 * @Autowired private DataSource dataSource;
 * @Autowired private RedisTemplate redisTemplate;
 * }
 * <p>
 * 3. 전체 스택 사용:
 * @SpringBootTest
 * @EnablePrimaveraTestcontainers({ContainerType.MARIADB, ContainerType.REDIS, ContainerType.KAFKA})
 * class MyIntegrationTest {
 * // 모든 인프라 컴포넌트 사용 가능
 * }
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(initializers = PrimaveraTestcontainersContextInitializer.class)
public @interface EnablePrimaveraTestcontainers {
    ContainerType[] value() default {ContainerType.MARIADB};

    int startupTimeout() default 60;

    boolean autoStop() default true;

    boolean reuse() default true;
}