package com.genius.primavera.testcontainer.annotation;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.config.ContainerConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 테스트에서 TestContainers를 활성화하는 애노테이션
 * 
 * @TestInstance(TestInstance.Lifecycle.PER_CLASS)와
 * @TestInstance(TestInstance.Lifecycle.PER_METHOD) 모두 지원
 * 
 * 사용 예:
 * <pre>
 * // 단일 컨테이너
 * @SpringBootTest
 * @EnableTestContainers(containers = ContainerType.MARIADB)
 * class MariaDBIntegrationTest {
 *     // MariaDB 컨테이너가 시작되고 DataSource가 구성됨
 * }
 * 
 * // 다중 컨테이너
 * @SpringBootTest
 * @EnableTestContainers(containers = {ContainerType.MARIADB, ContainerType.REDIS})
 * class MultiContainerTest {
 *     // MariaDB와 Redis 컨테이너가 모두 시작됨
 * }
 * 
 * // 기본값 (MariaDB)
 * @SpringBootTest
 * @EnableTestContainers
 * class DefaultTest {
 *     // MariaDB 컨테이너가 시작됨 (기본값)
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ContainerConfigurationSelector.class)
public @interface EnableTestContainers {
    
    /**
     * 활성화할 컨테이너 타입들
     * 기본값은 MariaDB
     */
    ContainerType[] containers() default {ContainerType.MARIADB};
    
    /**
     * 컨테이너별 초기화 스크립트 경로
     * 기본값은 "sql/init.sql"
     */
    String initScript() default "sql/init.sql";
    
    /**
     * 컨테이너 재사용 여부
     * true일 경우 동일한 컨테이너를 여러 테스트에서 재사용
     */
    boolean reuse() default false;
}