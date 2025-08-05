package com.genius.primavera.testcontainer.v3;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.*;

/**
 * TestContainer 활성화를 위한 메인 어노테이션 (v3)
 * 
 * <p>이 어노테이션은 다음과 같은 하이브리드 방식을 사용합니다:</p>
 * <ul>
 *   <li>어노테이션으로 컨테이너 타입과 이름 선언</li>
 *   <li>application.yml로 상세 설정 정의</li>
 *   <li>동적 Spring Bean 생성</li>
 *   <li>테스트 클래스 단위 격리</li>
 *   <li>JUnit 병렬 처리 지원</li>
 * </ul>
 * 
 * <h3>사용 예시:</h3>
 * <pre>
 * &#64;SpringBootTest
 * &#64;EnableTestContainer({
 *     &#64;TestContainer(type = ContainerType.MARIADB, name = "primaryDb"),
 *     &#64;TestContainer(type = ContainerType.MARIADB, name = "secondaryDb"),
 *     &#64;TestContainer(type = ContainerType.REDIS, name = "cache"),
 *     &#64;TestContainer(type = ContainerType.KAFKA, name = "messaging")
 * })
 * class MultiContainerTest {
 *     &#64;Autowired
 *     &#64;Qualifier("primaryDb")
 *     private DataSource primaryDataSource;
 *     
 *     &#64;Autowired
 *     &#64;Qualifier("cache")
 *     private RedisTemplate redisTemplate;
 * }
 * </pre>
 * 
 * <h3>application-test.yml 설정 예시:</h3>
 * <pre>
 * testcontainer:
 *   containers:
 *     primaryDb:
 *       image: "mariadb:11.4.7"
 *       database: "primary_db"
 *       username: "primary_user"
 *       password: "primary_pass"
 *       init-script: "sql/primary-init.sql"
 *     secondaryDb:
 *       image: "mariadb:11.4.7"
 *       database: "secondary_db"
 *       username: "secondary_user"
 *       password: "secondary_pass"
 *     cache:
 *       image: "redis:7-alpine"
 *       password: "redis_password"
 *     messaging:
 *       image: "confluentinc/cp-kafka:7.5.0"
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Testcontainers
@ExtendWith(TestContainerExtension.class)
@ContextConfiguration(initializers = TestContainerContextInitializer.class)
public @interface EnableTestContainer {
    
    /**
     * 테스트에서 사용할 컨테이너 선언
     */
    TestContainer[] value();
    
    /**
     * 개별 컨테이너 선언
     */
    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestContainer {
        /**
         * 컨테이너 타입
         */
        ContainerType type();
        
        /**
         * 컨테이너 이름 (Spring Bean 이름 및 application.yml 키로 사용)
         */
        String name();
    }
}