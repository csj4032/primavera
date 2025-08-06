package com.genius.primavera;

import com.genius.primavera.testcontainers.EnableTestContainers;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers
@DisplayName("DataAccessApplication Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessApplicationTest {

    @Test
    @Order(1)
    @DisplayName("Application Context Load Test")
    public void contextLoadsTest() {
        // 이 테스트 메소드의 본문은 비어 있어도 됩니다.
        // @SpringBootTest 어노테이션에 의해 애플리케이션 컨텍스트를 로딩하는 과정에서
        // 에러가 발생하면 테스트는 자동으로 실패합니다.
        // 성공적으로 이 메소드까지 실행되면, 컨텍스트 로딩이 성공했다는 의미입니다.
    }
}