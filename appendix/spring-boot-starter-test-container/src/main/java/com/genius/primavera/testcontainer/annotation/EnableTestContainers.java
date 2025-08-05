package com.genius.primavera.testcontainer.annotation;

import com.genius.primavera.testcontainer.ContainerSpec;
import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.config.ContainerConfigurationSelector;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 테스트에서 TestContainers를 활성화하는 어노테이션
 * 
 * ContainerSpec을 사용하여 다중 컨테이너 환경 지원
 * 각 컨테이너별로 이름, 설정, 역할을 명확히 구분
 *
 * 사용 예:
 * <pre>
 * // 기본 단일 컨테이너
 * @SpringBootTest
 * @EnableTestContainers
 * class DefaultTest {
 *     @Qualifier("primaryDataSource")
 *     @Autowired DataSource dataSource;
 * }
 *
 * // Primary/Secondary 다중 데이터베이스
 * @SpringBootTest
 * @EnableTestContainers(containers = {
 *     @ContainerSpec(type = ContainerType.MARIADB, name = "primary", initScript = "sql/primary.sql"),
 *     @ContainerSpec(type = ContainerType.MARIADB, name = "secondary", initScript = "sql/secondary.sql")
 * })
 * class MultiDatabaseTest {
 *     @Qualifier("primaryDataSource")
 *     @Autowired DataSource primaryDataSource;
 *     
 *     @Qualifier("secondaryDataSource") 
 *     @Autowired DataSource secondaryDataSource;
 * }
 *
 * // 혼합 컨테이너 (데이터베이스 + 캐시 + 검색)
 * @SpringBootTest
 * @EnableTestContainers(containers = {
 *     @ContainerSpec(type = ContainerType.MARIADB, name = "primary"),
 *     @ContainerSpec(type = ContainerType.REDIS, name = "cache"),
 *     @ContainerSpec(type = ContainerType.ELASTICSEARCH, name = "search")
 * })
 * class MixedContainerTest {
 *     @Qualifier("primaryDataSource") @Autowired DataSource database;
 *     @Qualifier("cacheRedisTemplate") @Autowired RedisTemplate<String, String> cache;
 *     @Qualifier("searchElasticsearchTemplate") @Autowired ElasticsearchTemplate search;
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ContainerConfigurationSelector.class)
public @interface EnableTestContainers {

    /**
     * 컨테이너 스펙 배열
     * 각 컨테이너의 타입, 이름, 설정을 정의
     * 기본값: MariaDB primary 컨테이너
     */
    ContainerSpec[] containers() default {
        @ContainerSpec(type = ContainerType.MARIADB, name = "primary")
    };

    /**
     * 웹 환경 설정 (테스트 격리 용도)
     * RANDOM_PORT를 사용하면 각 테스트가 독립적인 ApplicationContext를 가짐
     * 기본값: RANDOM_PORT
     */
    SpringBootTest.WebEnvironment webEnvironment() default SpringBootTest.WebEnvironment.RANDOM_PORT;
}