package com.genius.primavera.testcontainer.annotation;

import com.genius.primavera.testcontainer.config.TestContainersAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.annotation.*;

/**
 * 테스트에서 TestContainers를 활성화하는 애노테이션
 * 
 * @TestInstance(TestInstance.Lifecycle.PER_CLASS)와
 * @TestInstance(TestInstance.Lifecycle.PER_METHOD) 모두 지원
 * 
 * 사용 예:
 * <pre>
 * @SpringBootTest
 * @EnableTestContainers
 * class MyIntegrationTest {
 *     // 자동으로 MariaDB 컨테이너가 시작되고 DataSource가 구성됨
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(TestContainersAutoConfiguration.class)
public @interface EnableTestContainers {
}