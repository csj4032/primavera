package com.genius.primavera.testcontainer.annotation;

import com.genius.primavera.testcontainer.ContainerType;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

/**
 * TestContainers와 SpringBootTest를 통합한 메타 어노테이션
 * 
 * 이 어노테이션은 @SpringBootTest와 @EnableTestContainers를 결합하여
 * 더 간편하게 TestContainers 기반 통합 테스트를 작성할 수 있게 합니다.
 * 
 * 사용 예:
 * <pre>
 * // 기본 사용 (MariaDB, MOCK 환경)
 * @TestContainersTest
 * class DefaultIntegrationTest {
 *     // MariaDB 컨테이너가 자동으로 시작됨
 * }
 * 
 * // 웹 환경 격리
 * @TestContainersTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * class IsolatedWebTest {
 *     // 독립적인 ApplicationContext와 랜덤 포트로 실행
 * }
 * 
 * // 다중 컨테이너와 커스텀 설정
 * @TestContainersTest(
 *     containers = {ContainerType.MARIADB, ContainerType.REDIS},
 *     webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
 *     properties = {"spring.jpa.show-sql=true"}
 * )
 * class AdvancedIntegrationTest {
 *     // MariaDB와 Redis가 시작되고, 독립적인 웹 환경에서 실행
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@EnableTestContainers
@ActiveProfiles("test")
public @interface TestContainersTest {
    
    /**
     * 활성화할 컨테이너 타입들
     * @EnableTestContainers의 containers와 연결
     */
    @AliasFor(annotation = EnableTestContainers.class, attribute = "containers")
    ContainerType[] containers() default {ContainerType.MARIADB};
    
    /**
     * 웹 환경 설정
     * @SpringBootTest의 webEnvironment와 연결
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "webEnvironment")
    SpringBootTest.WebEnvironment webEnvironment() default SpringBootTest.WebEnvironment.MOCK;
    
    /**
     * 추가 프로퍼티 설정
     * @SpringBootTest의 properties와 연결
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
    String[] properties() default {};
    
    /**
     * 컨테이너 초기화 스크립트
     * @EnableTestContainers의 initScript와 연결
     */
    @AliasFor(annotation = EnableTestContainers.class, attribute = "initScript")
    String initScript() default "sql/init.sql";
    
    /**
     * 컨테이너 재사용 여부
     * @EnableTestContainers의 reuse와 연결
     */
    @AliasFor(annotation = EnableTestContainers.class, attribute = "reuse")
    boolean reuse() default false;
    
    /**
     * 테스트 클래스들
     * @SpringBootTest의 classes와 연결
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
    Class<?>[] classes() default {};
}